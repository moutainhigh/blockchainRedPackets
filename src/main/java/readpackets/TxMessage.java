package readpackets;

import chainmq.AbstractMessage;
import okhttp3.Call;
import okhttp3.Response;

import java.io.IOException;

public class TxMessage extends AbstractMessage {

    public TxMessage(String url, String cmd) {
        super(url, cmd);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        System.out.println(this);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        System.out.println(this);
    }
}