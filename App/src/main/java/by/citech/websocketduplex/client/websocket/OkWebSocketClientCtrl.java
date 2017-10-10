package by.citech.websocketduplex.client.websocket;

import android.util.Log;
import java.util.concurrent.TimeUnit;

import by.citech.websocketduplex.utils.Tags;
import okhttp3.WebSocket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;
import by.citech.websocketduplex.utils.Messages;
import by.citech.websocketduplex.utils.StatusMessages;
import okhttp3.WebSocket;
import okio.ByteString;

public class OkWebSocketClientCtrl implements IOkWebSocketClientListener {
    private WebSocket webSocket;
    private String status = "";
    private String url = "";

    public OkWebSocketClientCtrl(String url) {
        this.url = url;
    }

    public void run() {
        Log.i(Tags.WSOCKET_CLT, "run");
        OkWebSocketClient client = new OkWebSocketClient(true, 15000, this.url, this);
        Log.i(Tags.WSOCKET_CLT, "OkWebSocketClient instance created");
        client.run();
        Log.i(Tags.WSOCKET_CLT, "OkWebSocketClient instance runned");
    }

    public void cancel() {
        Log.i(Tags.WSOCKET_CLT, "cancel");
        webSocket.cancel();
    }

    public void stop(String reason) {
        Log.i(Tags.WSOCKET_CLT, "stop");
        webSocket.close(1000, reason);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        Log.i(Tags.WSOCKET_CLT, "onOpen");
        this.webSocket = webSocket;
        status = StatusMessages.WEBSOCKET_OPEN;
        webSocket.send(Messages.CLT2SRV_ONOPEN);
    }

    @Override
    public void onMessage(ByteString bytes) {
    }

    @Override
    public void onMessage(String text) {
    }

    @Override
    public void onClosing(int code, String reason) {
        status = StatusMessages.WEBSOCKET_CLOSING;
    }

    @Override
    public void onClosed(int code, String reason) {
        status = StatusMessages.WEBSOCKET_CLOSED;
    }

    @Override
    public void onFailure() {
    }

    public String getStatus() {
        return this.status;
    }

    public void sendMessage(String string) {
        Log.i(Tags.WSOCKET_CLT, "sendMessage");
        webSocket.send(string);
    }
}
