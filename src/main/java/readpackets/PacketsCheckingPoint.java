package readpackets;

import java.util.concurrent.DelayQueue;

public class PacketsCheckingPoint extends Thread {

    public static final PacketsCheckingPoint instance = new PacketsCheckingPoint();

    private final DelayQueue<PacketsTask> queue;

    private PacketsCheckingPoint() {
        super("PacketsCheckingPoint");
        queue = new DelayQueue<>();
        setPriority(Thread.MAX_PRIORITY);
        start();
    }

    public void add(PacketsTask packet) {
        queue.offer(packet);
    }

    public void remove(PacketsTask packet) {
        queue.remove(packet);
    }

    @Override
    public void run() {
        PacketsTask take;
        while (true) try {
            take = queue.take();
            take.remove();
        } catch (Exception e) {
        }
    }
}
