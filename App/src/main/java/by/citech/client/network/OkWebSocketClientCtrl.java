package by.citech.websocketduplex.client.network;

import android.os.Handler;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import by.citech.websocketduplex.param.Settings;
import by.citech.websocketduplex.param.Tags;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import by.citech.websocketduplex.param.Messages;
import by.citech.websocketduplex.param.StatusMessages;

import static by.citech.websocketduplex.util.Decode.bytesToHex;

public class OkWebSocketClientCtrl extends WebSocketListener {
    private WebSocket webSocket;
    private String status = "";
    private String url = "";
    private Handler handler;

    public OkWebSocketClientCtrl(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "run");
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Settings.readTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(Settings.connectTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(Settings.reconnect)
                .build();
        Request request = new Request.Builder()
//              .url("ws://echo.network.org")
                .url(url)
                .build();
        client.newWebSocket(request, this);
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    public void cancel() {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "cancel");

        if (webSocket != null) {
            webSocket.cancel();
            handler.sendEmptyMessage(StatusMessages.CLT_CANCEL);
        }
    }

    public void stop(String reason) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "stop");

        if (webSocket != null) {
            webSocket.close(1000, reason);
        }
    }

    public String getStatus() {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "getStatus");
        return this.status;
    }

    public void sendBytes(byte... bytes) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "sendBytes");
        Log.i(Tags.CLT_WSOCKETCTRL, String.format("sendBytes: bytes is <%s>", bytesToHex(bytes)));
        ByteString byteString = ByteString.of(bytes);
        Log.i(Tags.CLT_WSOCKETCTRL, String.format("sendBytes: byteString is <%s>", bytesToHex(byteString.toByteArray())));
        webSocket.send(byteString);
    }

    public void sendMessage(String string) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "sendMessage");
        webSocket.send(string);

        /*-------------------------- TEST --------------------------->>
        byte[] bytes = {0x2c, 0x56, 0x78, 0x7b};
        byte[] bytes = {(byte) 0x9a, (byte) 0x56, (byte) 0x78, (byte) 0xff};
        ByteString byteString = ByteString.of(bytes);
        webSocket.send(byteString);
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        <<-------------------------- TEST -----------------------------*/
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onOpen");
        this.webSocket = webSocket;
        status = StatusMessages.WEBSOCKET_OPENED;
        webSocket.send(Messages.CLT2SRV_ONOPEN);
        handler.sendEmptyMessage(StatusMessages.CLT_ONOPEN);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onMessage bytes");
        handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_BYTES, bytes).sendToTarget();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onMessage text");
        //handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_TEXT, text).sendToTarget();
    }

    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onClosing");
        webSocket.close(1000, Messages.CLT2SRV_ONCLOSE);
        status = StatusMessages.WEBSOCKET_CLOSING;
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSING);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onClosed");
        status = StatusMessages.WEBSOCKET_CLOSED;
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSED);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (Settings.debug) Log.i(Tags.CLT_WSOCKETCTRL, "onFailure");
        handler.sendEmptyMessage(StatusMessages.CLT_ONFAILURE);
    }
}
