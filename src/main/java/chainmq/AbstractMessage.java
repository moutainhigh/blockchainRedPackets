package chainmq;

import okhttp3.Callback;
import tools.IdUtil;

/**
 * 与链交互的消息的实体类
 */
public abstract class AbstractMessage implements Callback {

    protected String messageId;
    protected String url;
    protected String cmd;
    protected Throwable throwable;

    public AbstractMessage(String url, String cmd) {
        this(IdUtil.getInstance().getPkId(), url, cmd);
    }

    public AbstractMessage(String messageId, String url, String cmd) {
        this.messageId = messageId;
        this.url = url;
        this.cmd = cmd;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof AbstractMessage && this.messageId.equals(((AbstractMessage) obj).messageId);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getUrl() {
        return url;
    }

    public String getCmd() {
        return cmd;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}