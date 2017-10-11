package by.citech.websocketduplex.client.network;

import android.util.Log;

import by.citech.websocketduplex.util.Tags;
import okhttp3.WebSocket;
import okio.ByteString;
import by.citech.websocketduplex.util.Messages;
import by.citech.websocketduplex.util.StatusMessages;

public class OkWebSocketClientCtrl implements IOkWebSocketClientListener {
    private WebSocket webSocket;
    private String status = "";
    private String url = "";

    public OkWebSocketClientCtrl(String url) {
        this.url = url;
    }

    public void run() {
        Log.i(Tags.CLT_WSOCKETCTRL, "run");
        OkWebSocketClient client = new OkWebSocketClient(true, 15000, this.url, this);
        Log.i(Tags.CLT_WSOCKETCTRL, "OkWebSocketClient instance created");
        client.run();
        Log.i(Tags.CLT_WSOCKETCTRL, "OkWebSocketClient instance runned");
    }

    public void cancel() {
        Log.i(Tags.CLT_WSOCKETCTRL, "cancel");
        webSocket.cancel();
    }

    public void stop(String reason) {
        Log.i(Tags.CLT_WSOCKETCTRL, "stop");
        webSocket.close(1000, reason);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        Log.i(Tags.CLT_WSOCKETCTRL, "onOpen");
        this.webSocket = webSocket;
        status = StatusMessages.WEBSOCKET_OPENED;
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
        Log.i(Tags.CLT_WSOCKETCTRL, "sendMessage");
        webSocket.send(string);
    }
}
