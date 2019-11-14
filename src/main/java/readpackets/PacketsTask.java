package readpackets;

import tools.IdUtil;
import tools.TimeUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketsTask implements Runnable, Delayed {

    private static final Random generator = new Random();

    /**
     * 抢红包合约id
     */
    private String contractId;
    /**
     * 发起人钱包地址
     */
    private String from;
    /**
     * 发起人钱包私钥
     */
    private String key;
    /**
     * 红包金额
     */
    private BigInteger amount;
    /**
     * 剩余数量
     */
    private BigInteger lastAmount;
    /**
     * 红包数量
     */
    private int packetSize;
    /**
     * 剩余数量
     */
    private int lastSize;
    /**
     * 参与抢红包的用户
     */
    private String[] participants;
    /**
     * 红包超时时间
     */
    private long outTime;
    /**
     * 留言
     */
    private String works;
    /**
     * 领取记录
     */
    private HashMap<String, BigInteger> members;
    /**
     * 等待抢红包用户
     */
    private LinkedBlockingQueue<String> waiter;
    /**
     * 抢红包状态
     */
    private AtomicBoolean status;

    private PacketsTask(String from, String key, BigInteger amount, int packetSize, String[] participants, long outTime, String works) {
        this.contractId = IdUtil.getInstance().getPkId();
        this.from = from;
        this.key = key;
        this.amount = amount;
        this.lastAmount = amount;
        this.packetSize = packetSize;
        this.lastSize = packetSize;
        this.participants = participants;
        this.outTime = outTime + TimeUtil.currentTimeMillis();
        this.works = works;
        this.members = new HashMap<>();
        this.waiter = new LinkedBlockingQueue<>();
        this.status = new AtomicBoolean(true);
    }

    public void addWaiter(String addr) {
        if (! waiter.contains(addr)) {
            waiter.offer(addr);
        }
    }

    @Override
    public void run() {
        try {
            if (status.getAndSet(false)) {
                System.out.println(this);
                while (true) {
                    String addr = waiter.poll(30000, TimeUnit.MILLISECONDS);
                    if (addr == null) {
                        return;
                    }
                    if (! isLast() || ! isTimeOut()) {
                        remove();
                        return;
                    }
                    if (! isContains(addr) || members.containsKey(addr)) {
                        continue;
                    }

                    BigInteger amount = randomAmount();
                    adjustLast(amount);
                    send(addr, amount);
                    System.out.println(addr + " <> " + amount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            status.set(true);
        }
    }

    /**
     * 扣除红包金额
     */
    private void adjustLast(BigInteger deduction) {
        lastSize--;
        lastAmount = lastAmount.subtract(deduction);
    }

    /**
     * 发送数据上链
     */
    private void send(String addr, BigInteger amount) {
        members.put(addr, amount);
        System.out.println("发送数据上链");
    }

    /**
     * 随机金额
     */
    private BigInteger randomAmount() {
        if (lastSize == 1) {
            return lastAmount;
        }
        if (lastAmount.intValue() == lastSize) {
            return BigInteger.ONE;
        }
        BigInteger last = lastAmount.subtract(BigInteger.valueOf(lastSize));
        int r = generator.nextInt(last.intValue()) + 1;
        return BigInteger.valueOf(r);
    }

    /**
     * 检查余额是否足够
     */
    private boolean isLast() {
        return lastSize > 0 && lastAmount.compareTo(BigInteger.ZERO) > 0;
    }

    private boolean isTimeOut() {
        return outTime > TimeUtil.currentTimeMillis();
    }

    /**
     * 检查用户是否带有参与抢红包的权限
     */
    private boolean isContains(String par) {
        for (int i = 0, size = participants.length; i < size; i++) {
            if (participants[i].equals(par)) {
                return true;
            }
        }
        return false;
    }

    public String getContractId() {
        return contractId;
    }

    public boolean isRun() {
        return ! status.get();
    }

    public void remove() {
        PacketsPool.instance.remove(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof PacketsTask && this.contractId.equals(((PacketsTask) obj).contractId);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(outTime - TimeUtil.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof PacketsTask) {
            return Long.compare(this.outTime, ((PacketsTask) o).outTime);
        }
        return 0;
    }

    public static PacketsTask build(String from, String key, BigInteger amount, int packetSize, String[] participants, long outTime, String works) {
        if (amount.intValue() >= packetSize) {
            return new PacketsTask(from, key, amount, packetSize, participants, outTime, works);
        }
        throw new IllegalArgumentException("parameter error");
    }
}