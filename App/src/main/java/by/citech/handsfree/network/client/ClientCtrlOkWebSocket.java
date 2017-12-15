package by.citech.handsfree.network.client;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import by.citech.handsfree.param.Messages;
import by.citech.handsfree.param.StatusMessages;

import static by.citech.handsfree.util.Decode.bytesToHexMark1;

public class ClientCtrlOkWebSocket
        extends WebSocketListener
        implements IClientCtrl, ITransmitter, IReceiverReg {

    private static final String TAG = Tags.CLT_WSOCKETCTRL;
    private static final boolean debug = Settings.debug;

    private WebSocket webSocket;
    private String status = "";
    private String url = "";
    private Handler handler;
    private IReceiver listener;

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
                .retryOnConnectionFailure(Settings.reconnectOnFail)
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
    public IReceiverReg getReceiverReg() {
        return this;
    }

    @Override
    public String getStatus() {
        if (debug) Log.i(TAG, "getStatus");
        return this.status;
    }

    //--------------------- IReceiverReg

    @Override
    public void registerReceiver(IReceiver listener) {
        if (debug) Log.i(TAG, "registerReceiver");
        this.listener = listener;
    }

    //--------------------- ITransmitter

    @Override
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData");
        if (debug) Log.i(TAG, "sendData " + bytesToHexMark1(data));
        webSocket.send(ByteString.of(data));
    }

    @Override
    public void sendData(short[] data) {
        Log.e(TAG, "sendData short[]");
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
        if (debug) Log.i(TAG, "onMessage " + bytesToHexMark1(bytes.toByteArray()));
        if (listener != null) {
            if (debug) Log.i(TAG, "onMessage redirecting");
            listener.onReceiveData(bytes.toByteArray());
        } else {
            if (debug) Log.i(TAG, "onMessage not redirecting");
        }
//      handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_BYTES, bytes).sendToTarget();
        handler.sendEmptyMessage(StatusMessages.CLT_ONMESSAGE_BYTES);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (debug) Log.i(TAG, "onMessage text");
        if (debug) Log.i(TAG, "onMessage " + text);
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
