package by.citech.network.client.connection;

import android.os.Handler;
import android.util.Log;
import java.util.concurrent.TimeUnit;

import by.citech.network.control.IReceiverListener;
import by.citech.network.control.IReceiverListenerReg;
import by.citech.network.control.ITransmitter;
import by.citech.param.Settings;
import by.citech.param.Tags;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import by.citech.param.Messages;
import by.citech.param.StatusMessages;

import static by.citech.util.Decode.bytesToHexMark1;

public class ClientCtrlOkWebSocket extends WebSocketListener implements IClientCtrl, ITransmitter, IReceiverListenerReg {
    private static final String TAG = Tags.CLT_WSOCKETCTRL;
    private static final boolean debug = Settings.debug;
    private WebSocket webSocket;
    private String status = "";
    private String url = "";
    private Handler handler;
    private IReceiverListener listener;

    public ClientCtrlOkWebSocket(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    //--------------------- IConnCtrl

    @Override
    public void closeConnection() {
        if (debug) Log.i(TAG, "closeConnection");
        if (webSocket != null) {
            webSocket.close(1000, "user manually closed connection");
        }
    }

    @Override
    public void closeConnectionForce() {
        if (debug) Log.i(TAG, "closeConnectionForce");
        if (webSocket != null) {
            webSocket.cancel();
            handler.sendEmptyMessage(StatusMessages.CLT_CANCEL);
        }
    }

    @Override
    public boolean isAliveConnection() {
        if (debug) Log.i(TAG, "isAliveConnection");
        // TODO: достаточна ли такая проверка?
        return status.equals(StatusMessages.WEBSOCKET_OPENED);
    }

    //--------------------- IClientCtrl

    @Override
    public IClientCtrl startClient() {
        if (debug) Log.i(TAG, "startClient");
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Settings.clientReadTimeout, TimeUnit.MILLISECONDS)
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
        return this;
    }

    @Override
    public ITransmitter getTransmitter() {
        return this;
    }

    @Override
    public IReceiverListenerReg getReceiverRegister() {
        return this;
    }

    @Override
    public String getStatus() {
        if (debug) Log.i(TAG, "getStatus");
        return this.status;
    }

    //--------------------- IReceiverListenerReg

    @Override
    public void registerReceiverListener(IReceiverListener listener) {
        if (debug) Log.i(TAG, "registerReceiverListener");
        this.listener = listener;
    }

    //--------------------- ITransmitter

    @Override
    public void sendBytes(byte... bytes) {
        if (debug) Log.i(TAG, "sendBytes");
        if (debug) {
            ByteString byteString = ByteString.of(bytes);
            Log.i(TAG, String.format("sendBytes: bytes is <%s>", bytesToHexMark1(bytes)));
            Log.i(TAG, String.format("sendBytes: byteString is <%s>", bytesToHexMark1(byteString.toByteArray())));
            webSocket.send(byteString);
        } else {
            webSocket.send(ByteString.of(bytes));
        }
    }

    @Override
    public void sendMessage(String string) {
        if (debug) Log.i(TAG, "sendMessage");
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
        if (debug) Log.i(TAG, "onOpen");
        this.webSocket = webSocket;
        status = StatusMessages.WEBSOCKET_OPENED;
        webSocket.send(Messages.CLT2SRV_ONOPEN);
        if (debug) Log.i(TAG, "onOpen message sended");
        handler.sendEmptyMessage(StatusMessages.CLT_ONOPEN);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (debug) Log.i(TAG, "onMessage bytes");
        if (listener == null) {
            if (debug) Log.i(TAG, "onMessage listener is null");
        } else {
            if (debug) Log.i(TAG, "onMessage listener is not null");
            listener.onReceiveMessage(bytes.toByteArray());
        }
//      handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_BYTES, bytes).sendToTarget();
        handler.sendEmptyMessage(StatusMessages.CLT_ONMESSAGE_BYTES);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (debug) Log.i(TAG, "onMessage text");
        handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_TEXT, text).sendToTarget();
    }

    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        if (debug) Log.i(TAG, "onClosing");
        webSocket.close(1000, Messages.CLT2SRV_ONCLOSE);
        status = StatusMessages.WEBSOCKET_CLOSING;
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSING);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (debug) Log.i(TAG, "onClosed");
        status = StatusMessages.WEBSOCKET_CLOSED;
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSED);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (debug) Log.i(TAG, "onFailure");
        handler.sendEmptyMessage(StatusMessages.CLT_ONFAILURE);
    }
}
