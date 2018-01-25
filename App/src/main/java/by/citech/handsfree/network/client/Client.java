package by.citech.handsfree.network.client;

import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import by.citech.handsfree.common.EConnectionState;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import by.citech.handsfree.parameters.Messages;
import by.citech.handsfree.parameters.StatusMessages;

public class Client
        extends WebSocketListener
        implements IClientCtrl, IRxComplex {

    private static final String STAG = Tags.Client;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    private OkHttpClient client;
    private WebSocket webSocket;
    private String url;
    private Handler handler;
    private IRxComplex receiver;
    private EConnectionState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        url = "";
        state = EConnectionState.Null;
    }

    Client(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    //--------------------- IClientCtrl

    @Override
    public IClientCtrl startClient() {
        if (debug) Log.i(TAG, "startClient");
        client = new OkHttpClient.Builder()
                .readTimeout(Settings.Network.clientReadTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(Settings.Network.connectTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(Settings.Network.reconnectOnFail)
                .build();
        Request request = new Request.Builder()
//              .url("ws://echo.network.org")
                .url(url)
                .build();
        client.newWebSocket(request, this);
        procState(EConnectionState.Opening);
        // Trigger shutdown of the dispatcher's executor so this applyPrefsToSettings can exit cleanly.
        client.dispatcher().executorService().shutdown();
        return this;
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
        return state == EConnectionState.Opened || state == EConnectionState.Opening;
    }

    //--------------------- IExchangeCtrl

    @Override
    public IRxComplex getTransmitter() {
        if (debug) Log.i(TAG, "getTransmitter");
        return this;
    }

    @Override
    public void setReceiver(IRxComplex iRxComplex) {
        if (debug) Log.i(TAG, "setReceiver");
        this.receiver = iRxComplex;
    }

    //--------------------- IRxComplex

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

    private void procState(EConnectionState state) {
        if (debug) Log.i(TAG, String.format(
                "procState from %s to %s, connections count is %d",
                this.state.name(), state.name(), client.connectionPool().connectionCount()));
        this.state = state;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (debug) Log.i(TAG, "onOpen");
        this.webSocket = webSocket;
        procState(EConnectionState.Opened);
        webSocket.send(Messages.CLT2SRV_ONOPEN);
        if (debug) Log.i(TAG, "onOpen message sended: " + Messages.CLT2SRV_ONOPEN);
        handler.sendEmptyMessage(StatusMessages.CLT_ONOPEN);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (debug) Log.i(TAG, String.format(Locale.US,
                "onMessage received bytes: %d bytes, to hex: %s",
                bytes.size(), bytes.hex()));
        if (receiver != null) {
            if (debug) Log.i(TAG, "onMessage redirecting");
            receiver.sendData(bytes.toByteArray());
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
        procState(EConnectionState.Closing);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSING);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (debug) Log.i(TAG, "onClosed");
        procState(EConnectionState.Closed);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSED);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (debug) Log.i(TAG, "onFailure");
        procState(EConnectionState.Failure);
        handler.sendEmptyMessage(StatusMessages.CLT_ONFAILURE);
    }

}
