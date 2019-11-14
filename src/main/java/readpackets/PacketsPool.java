package readpackets;

import java.util.concurrent.*;

public class PacketsPool extends Thread {

    public static final PacketsPool instance = new PacketsPool();

    private final LinkedBlockingQueue<Runnable> queue;
    private final ConcurrentHashMap<String, PacketsTask> task;
    private final ExecutorService executor;

    public PacketsPool() {
        super("PacketsPool");
        queue = new LinkedBlockingQueue<>();
        task = new ConcurrentHashMap<>();
        executor = new ThreadPoolExecutor(0, 1000, 60000L, TimeUnit.MILLISECONDS, queue, r -> new Thread(r, "PacketsPoolExecute"));
        setPriority(Thread.MAX_PRIORITY);
        start();
    }

    public void competition(String addr, String contractId) {
        PacketsTask task = this.task.get(contractId);
        if (task != null) {
            task.addWaiter(addr);
            if (! task.isRun() && ! queue.contains(task)) {
                queue.offer(task);
            }
        }
    }

    public void add(PacketsTask packet) {
        task.putIfAbsent(packet.getContractId(), packet);
        if (! packet.isRun() && ! queue.contains(task)) {
            queue.offer(packet);
            PacketsCheckingPoint.instance.add(packet);
        }
    }

    public void remove(PacketsTask packet) {
        task.remove(packet);
        queue.remove(packet);
    }

    @Override
    public void run() {
        PacketsTask task;
        while (true) try {
            task = (PacketsTask) queue.take();
            if (! task.isRun()) {
                executor.execute(task);
            }
        } catch (Exception e) {
        }
    }
}
