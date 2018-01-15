package by.citech.handsfree.network.server;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.citech.handsfree.common.EConnectionState;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.network.server.connection.protocols.http.IHTTPSession;
import by.citech.handsfree.network.server.connection.websockets.CloseCode;
import by.citech.handsfree.network.server.connection.websockets.NanoWSD;
import by.citech.handsfree.network.server.connection.websockets.WebSocket;
import by.citech.handsfree.network.server.connection.websockets.WebSocketFrame;
import by.citech.handsfree.parameters.Messages;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public class Server
        extends NanoWSD
        implements IServerCtrl, ITransmitter {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private static final String STAG = Tags.Server;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    private WebSocket webSocket;
    private Handler handler;
    private ITransmitter receiver;
    private EConnectionState state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        state = EConnectionState.Null;
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
        if (debug) Log.i(TAG, "startServer");
        start(serverTimeout);
        return this;
    }

    @Override
    public boolean isAliveServer() {
        boolean isAliveServer = isAlive();
        if (debug) Log.i(TAG, "isAliveServer is alive: " + isAliveServer);
        return isAliveServer;
    }

    @Override
    public void stopServer() {
        if (debug) Log.i(TAG, "stopServer");
        stop();
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
        try {
            webSocket.send(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (message == null || webSocket == null) {
            if (debug) Log.i(TAG, "sendMessage message or websocket is null");
            return;
        }
        try {
            webSocket.send(message);
            if (debug) Log.i(TAG, "sendMessage sended: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //--------------------- IExchangeCtrl

    @Override
    public ITransmitter getTransmitter() {
        if (debug) Log.i(TAG, "getTransmitter");
        return this;
    }

    @Override
    public void setReceiver(ITransmitter iTransmitter) {
        if (debug) Log.i(TAG, "setReceiver");
        this.receiver = iTransmitter;
    }

    //--------------------- IConnCtrl

    @Override
    public void closeConnection() {
        if (debug) Log.i(TAG, "closeConnection");
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
        if (debug) Log.i(TAG, "closeConnectionForce");
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

    private void procState(EConnectionState state) {
        if (debug) Log.i(TAG, String.format(
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
            if (debug) Log.i(TAG, "onOpen");
            procState(EConnectionState.Opened);
            handler.sendEmptyMessage(StatusMessages.SRV_ONOPEN);
            try {
                send(Messages.SRV2CLT_ONOPEN);
                if (debug) Log.i(TAG, "onOpen message sended: " + Messages.SRV2CLT_ONOPEN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (debug) Log.i(TAG, "onClose");
            if (debug) Log.i(TAG,
                    "Initiated by " + (initiatedByRemote ? "remote. " : "self. ") +
                    "Close code is <" + code + ">. " +
                    "Reason is <" + reason + ">.");
            procState(EConnectionState.Closed);
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            byte[] receivedData = message.getBinaryPayload();
            if (debug) Log.i(TAG, String.format(Locale.US,
                    "onMessage received bytes: %d bytes, toString: %s",
                    receivedData.length, Arrays.toString(receivedData)));
            if (receiver != null) {
                if (debug) Log.i(TAG, "onMessage redirecting");
                receiver.sendData(receivedData);
            } else {
                if (debug) Log.i(TAG, "onMessage not redirecting");
                handler.sendEmptyMessage(StatusMessages.SRV_ONMESSAGE);
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (debug) Log.i(TAG, "onPong " + pong);
            handler.sendEmptyMessage(StatusMessages.SRV_ONPONG);
        }

        @Override
        protected void onException(IOException exception) {
            if (debug) Log.i(TAG, "onException " + exception.getMessage());
            if (debug) Server.LOG.log(Level.SEVERE, "exception occured", exception);
            procState(EConnectionState.Failure);
            handler.sendEmptyMessage(StatusMessages.SRV_ONFAILURE);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (debug) Log.i(TAG, "debugFrameReceived");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMERX);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (debug) Log.i(TAG, "debugFrameSent");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMETX);
        }

    }

}
