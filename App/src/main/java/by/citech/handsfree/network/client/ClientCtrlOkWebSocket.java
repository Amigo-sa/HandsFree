package by.citech.handsfree.network.client;

import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import by.citech.handsfree.common.EConnection;
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

    private static final String STAG = Tags.CLT_WSOCKETCTRL;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    private OkHttpClient client;
    private WebSocket webSocket;
    private String url;
    private Handler handler;
    private IReceiver listener;
    private EConnection state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        url = "";
        state = EConnection.Null;
    }

    ClientCtrlOkWebSocket(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    //--------------------- state

    private void procState(EConnection state) {
        if (debug) Log.i(TAG, String.format(
                "procState from %s to %s, connections count is %d",
                this.state.name(), state.name(), client.connectionPool().connectionCount()));
        this.state = state;
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
        // TODO: достаточна ли такая проверка?
        return state == EConnection.Opened || state == EConnection.Opening;
    }

    //--------------------- IClientCtrl

    @Override
    public IClientCtrl startClient() {
        if (debug) Log.i(TAG, "startClient");
        client = new OkHttpClient.Builder()
                .readTimeout(Settings.clientReadTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(Settings.connectTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(Settings.reconnectOnFail)
                .build();
        Request request = new Request.Builder()
//              .url("ws://echo.network.org")
                .url(url)
                .build();
        client.newWebSocket(request, this);
        procState(EConnection.Opening);
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
        return this;
    }

    @Override
    public ITransmitter getTransmitter() {
        if (debug) Log.i(TAG, "getTransmitter");
        return this;
    }

    @Override
    public IReceiverReg getReceiverReg() {
        if (debug) Log.i(TAG, "getReceiverReg");
        return this;
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
        if (data == null || webSocket == null) {
            if (debug) Log.i(TAG, "sendData data or websocket is null");
            return;
        }
        if (debug) Log.i(TAG, String.format(
                "sendData: %d bytes, toString: %s",
                data.length, Arrays.toString(data)));
        webSocket.send(ByteString.of(data));
    }

    @Override
    public void sendMessage(String message) {
        if (message == null || webSocket == null) {
            if (debug) Log.i(TAG, "sendMessage message or websocket is null");
            return;
        }
        webSocket.send(message);
        if (debug) Log.i(TAG, "sendMessage sended: " + message);
    }

    //--------------------- main

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (debug) Log.i(TAG, "onOpen");
        this.webSocket = webSocket;
        procState(EConnection.Opened);
        webSocket.send(Messages.CLT2SRV_ONOPEN);
        if (debug) Log.i(TAG, "onOpen message sended: " + Messages.CLT2SRV_ONOPEN);
        handler.sendEmptyMessage(StatusMessages.CLT_ONOPEN);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (debug) Log.i(TAG, String.format(Locale.US,
                "onMessage received bytes: %d bytes, to hex: %s",
                bytes.size(), bytes.hex()));
        if (listener != null) {
            if (debug) Log.i(TAG, "onMessage redirecting");
            listener.onReceiveData(bytes.toByteArray());
        } else {
            if (debug) Log.i(TAG, "onMessage not redirecting");
            handler.sendEmptyMessage(StatusMessages.CLT_ONMESSAGE_BYTES);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (debug) Log.i(TAG, "onMessage received text: " + text);
        handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_TEXT, text).sendToTarget();
    }

    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        if (debug) Log.i(TAG, "onClosing");
        webSocket.close(1000, Messages.CLT2SRV_ONCLOSE);
        procState(EConnection.Closing);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSING);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (debug) Log.i(TAG, "onClosed");
        procState(EConnection.Closed);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSED);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (debug) Log.i(TAG, "onFailure");
        procState(EConnection.Failure);
        handler.sendEmptyMessage(StatusMessages.CLT_ONFAILURE);
    }

}
