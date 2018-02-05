package by.citech.handsfree.network.server;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.citech.handsfree.common.ELinkState;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.network.server.connection.protocols.http.IHTTPSession;
import by.citech.handsfree.network.server.connection.websockets.CloseCode;
import by.citech.handsfree.network.server.connection.websockets.NanoWSD;
import by.citech.handsfree.network.server.connection.websockets.WebSocket;
import by.citech.handsfree.network.server.connection.websockets.WebSocketFrame;
import by.citech.handsfree.parameters.Messages;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class Server
        extends NanoWSD
        implements IServerCtrl, IRxComplex {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private static final String STAG = Tags.Server;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    private WebSocket webSocket;
    private Handler handler;
    private IRxComplex receiver;
    private ELinkState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        state = ELinkState.Null;
    }

    Server(int port, Handler handler) {
        super(port);
        this.handler = handler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        webSocket = new DebugWebSocket(handshake);
        return webSocket;
    }

    //--------------------- IServerCtrl

    @Override
    public IServerCtrl startServer(int serverTimeout) throws IOException {
        Timber.i("startServer");
        start(serverTimeout);
        return this;
    }

    @Override
    public boolean isAliveServer() {
        boolean isAliveServer = isAlive();
        Timber.i("isAliveServer is alive: " + isAliveServer);
        return isAliveServer;
    }

    @Override
    public void stopServer() {
        Timber.i("stopServer");
        stop();
    }

    //--------------------- IRxComplex

    @Override
    public void sendData(byte[] data) {
        if (data == null || webSocket == null) {
            Timber.i("sendData data or websocket is null");
            return;
        }
        Timber.i(String.format(
                "sendData: %d bytes, toString: %s",
                data.length, Arrays.toString(data)));
        try {
            webSocket.send(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (message == null || webSocket == null) {
            Timber.i("sendMessage message or websocket is null");
            return;
        }
        try {
            webSocket.send(message);
            Timber.i("sendMessage sended: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //--------------------- IExchangeCtrl

    @Override
    public IRxComplex getTransmitter() {
        Timber.i("getTransmitter");
        return this;
    }

    @Override
    public void setReceiver(IRxComplex iRxComplex) {
        Timber.i("setReceiver");
        this.receiver = iRxComplex;
    }

    //--------------------- IConnCtrl

    @Override
    public void closeConnection() {
        Timber.i("closeConnection");
        if (webSocket != null) {
            try {
                webSocket.close(CloseCode.NormalClosure, Messages.SRV2CLT_ONCLOSE, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeConnectionForce() {
        Timber.i("closeConnectionForce");
        if (webSocket != null) {
            try {
                webSocket.close(CloseCode.AbnormalClosure, Messages.SRV2CLT_ONCLOSE, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isAliveConnection() {
        return webSocket != null && webSocket.isOpen();
    }

    //--------------------- main

    private void procState(ELinkState state) {
        Timber.i(String.format(
                "procState from %s to %s",
                this.state.name(), state.name()));
        this.state = state;
    }

    //  private static class DebugWebSocket extends WebSocket {
    private class DebugWebSocket extends WebSocket {

        private DebugWebSocket(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            Timber.i("onOpen");
            procState(ELinkState.Opened);
            handler.sendEmptyMessage(StatusMessages.SRV_ONOPEN);
            try {
                send(Messages.SRV2CLT_ONOPEN);
                Timber.i("onOpen message sended: " + Messages.SRV2CLT_ONOPEN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            Timber.i("onClose");
            Timber.i(
                    "Initiated by " + (initiatedByRemote ? "remote. " : "self. ") +
                    "Close code is <" + code + ">. " +
                    "Reason is <" + reason + ">.");
            procState(ELinkState.Closed);
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            byte[] receivedData = message.getBinaryPayload();
            Timber.i(String.format(Locale.US,
                    "onMessage received bytes: %d bytes, toString: %s",
                    receivedData.length, Arrays.toString(receivedData)));
            if (receiver != null) {
                Timber.i("onMessage redirecting");
                receiver.sendData(receivedData);
            } else {
                Timber.i("onMessage not redirecting");
                handler.sendEmptyMessage(StatusMessages.SRV_ONMESSAGE);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            Timber.i("onPong " + pong);
            handler.sendEmptyMessage(StatusMessages.SRV_ONPONG);
        }

        @Override
        protected void onException(IOException exception) {
            Timber.i("onException " + exception.getMessage());
            if (debug) Server.LOG.log(Level.SEVERE, "exception occured", exception);
            procState(ELinkState.Failure);
            handler.sendEmptyMessage(StatusMessages.SRV_ONFAILURE);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            Timber.i("debugFrameReceived");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMERX);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            Timber.i("debugFrameSent");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMETX);
        }

    }

}
