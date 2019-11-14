import readpackets.PacketsPool;
import readpackets.PacketsTask;

import java.math.BigInteger;

public class TestPackets {

    public static void main(String[] args) {
        PacketsTask task = PacketsTask.build
                ("public", "private", BigInteger.valueOf(2), 2, new String[]{"1", "2", "3"}, 30000, "新年快乐");
        PacketsPool.instance.add(task);

        PacketsPool.instance.competition("8", task.getContractId());
        PacketsPool.instance.competition("1", task.getContractId());
        PacketsPool.instance.competition("1", task.getContractId());
        PacketsPool.instance.competition("2", task.getContractId());
        PacketsPool.instance.competition("3", task.getContractId());
        PacketsPool.instance.competition("5", task.getContractId());
    }
}