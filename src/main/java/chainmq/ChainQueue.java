package chainmq;

import okhttp3.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 与链交互的消息队列
 */
public final class ChainQueue extends Thread {

    public static final ChainQueue instance = new ChainQueue();

    private final LinkedBlockingQueue<AbstractMessage> queue;
    private final OkHttpClient client;

    private ChainQueue() {
        super("ChainQueue");
        queue = new LinkedBlockingQueue<>();
        client = new OkHttpClient();
        setPriority(Thread.MAX_PRIORITY);
        start();
    }

    public void addMessage(AbstractMessage message) {
        if (! queue.contains(message)) {
            queue.offer(message);
        }
    }

    @Override
    public synchronized void run() {
        AbstractMessage message;
        Request request;
        while (true) try {
            message = queue.take();
            request = new Request.Builder().url(message.url)
                    .post(RequestBody.create(message.cmd.getBytes(StandardCharsets.UTF_8))).build();
            client.newCall(request).enqueue(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}