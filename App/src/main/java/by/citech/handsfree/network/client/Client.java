package by.citech.handsfree.network.client;

import android.os.Handler;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import by.citech.handsfree.common.ELinkState;
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
import timber.log.Timber;

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
    private ELinkState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        url = "";
        state = ELinkState.Null;
    }

    Client(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    //--------------------- IClientCtrl

    @Override
    public IClientCtrl startClient() {
        if (debug) Timber.tag(TAG).i("startClient");
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
        procState(ELinkState.Opening);
        // Trigger shutdown of the dispatcher's executor so this applyPrefsToSettings can exit cleanly.
        client.dispatcher().executorService().shutdown();
        return this;
    }

    //--------------------- IConnCtrl

    @Override
    public void closeConnection() {
        if (debug) Timber.tag(TAG).i("closeConnection");
        if (webSocket != null) {
            webSocket.close(1000, "user manually closed connection");
        }
    }

    @Override
    public void closeConnectionForce() {
        if (debug) Timber.tag(TAG).i("closeConnectionForce");
        if (webSocket != null) {
            webSocket.cancel();
            handler.sendEmptyMessage(StatusMessages.CLT_CANCEL);
        }
    }

    @Override
    public boolean isAliveConnection() {
        // TODO: достаточна ли такая проверка?
        return state == ELinkState.Opened || state == ELinkState.Opening;
    }

    //--------------------- IExchangeCtrl

    @Override
    public IRxComplex getTransmitter() {
        if (debug) Timber.tag(TAG).i("getTransmitter");
        return this;
    }

    @Override
    public void setReceiver(IRxComplex iRxComplex) {
        if (debug) Timber.tag(TAG).i("setReceiver");
        this.receiver = iRxComplex;
    }

    //--------------------- IRxComplex

    @Override
    public void sendData(byte[] data) {
        if (data == null || webSocket == null) {
            if (debug) Timber.tag(TAG).i("sendData data or websocket is null");
            return;
        }
        if (debug) Timber.tag(TAG).i("sendData: %d bytes, toString: %s",
                data.length, Arrays.toString(data));
        webSocket.send(ByteString.of(data));
    }

    @Override
    public void sendMessage(String message) {
        if (message == null || webSocket == null) {
            if (debug) Timber.tag(TAG).i("sendMessage message or websocket is null");
            return;
        }
        webSocket.send(message);
        if (debug) Timber.tag(TAG).i("sendMessage sended: %s", message);
    }

    //--------------------- main

    private void procState(ELinkState state) {
        if (debug) Timber.tag(TAG).i("procState from %s to %s, connections count is %d",
                this.state.name(), state.name(), client.connectionPool().connectionCount());
        this.state = state;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (debug) Timber.tag(TAG).i("onOpen");
        this.webSocket = webSocket;
        procState(ELinkState.Opened);
        webSocket.send(Messages.CLT2SRV_ONOPEN);
        if (debug) Timber.tag(TAG).i("onOpen message sended: %s", Messages.CLT2SRV_ONOPEN);
        handler.sendEmptyMessage(StatusMessages.CLT_ONOPEN);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (debug) Timber.tag(TAG).i("onMessage received bytes: %d bytes, to hex: %s",
                bytes.size(), bytes.hex());
        if (receiver != null) {
            if (debug) Timber.tag(TAG).i("onMessage redirecting");
            receiver.sendData(bytes.toByteArray());
        } else {
            if (debug) Timber.tag(TAG).i("onMessage not redirecting");
            handler.sendEmptyMessage(StatusMessages.CLT_ONMESSAGE_BYTES);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (debug) Timber.tag(TAG).i("onMessage received text: %s", text);
        handler.obtainMessage(StatusMessages.CLT_ONMESSAGE_TEXT, text).sendToTarget();
    }

    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        if (debug) Timber.tag(TAG).i("onClosing");
        webSocket.close(1000, Messages.CLT2SRV_ONCLOSE);
        procState(ELinkState.Closing);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSING);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (debug) Timber.tag(TAG).i("onClosed");
        procState(ELinkState.Closed);
        handler.sendEmptyMessage(StatusMessages.CLT_ONCLOSED);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (debug) Timber.tag(TAG).i("onFailure");
        procState(ELinkState.Failure);
        handler.sendEmptyMessage(StatusMessages.CLT_ONFAILURE);
    }

}
