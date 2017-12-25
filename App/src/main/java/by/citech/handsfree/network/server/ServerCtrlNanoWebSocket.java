package by.citech.handsfree.network.server;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.citech.handsfree.common.EConnection;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.network.server.connection.protocols.http.IHTTPSession;
import by.citech.handsfree.network.server.connection.websockets.CloseCode;
import by.citech.handsfree.network.server.connection.websockets.NanoWSD;
import by.citech.handsfree.network.server.connection.websockets.WebSocket;
import by.citech.handsfree.network.server.connection.websockets.WebSocketFrame;
import by.citech.handsfree.param.Messages;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

import static by.citech.handsfree.util.Decode.bytesToHexMark1;

public class ServerCtrlNanoWebSocket
        extends NanoWSD
        implements IServerCtrl, IReceiverReg, ITransmitter {

    private static final Logger LOG = Logger.getLogger(ServerCtrlNanoWebSocket.class.getName());

    private static final String STAG = Tags.SRV_WSOCKETCTRL;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    private WebSocket webSocket;
    private Handler handler;
    private IReceiver listener;
    private EConnection state;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        state = EConnection.Null;
    }

    ServerCtrlNanoWebSocket(int port, Handler handler) {
        super(port);
        this.handler = handler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        webSocket = new DebugWebSocket(handshake);
        return webSocket;
    }

    //--------------------- state

    private void procState(EConnection state) {
        if (debug) Log.i(TAG, String.format(
                "procState from %s to %s",
                this.state.name(), state.name()));
        this.state = state;
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
    public IReceiverReg getReceiverReg() {
        if (debug) Log.i(TAG, "getReceiverReg");
        return this;
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

    //--------------------- IReceiverReg

    @Override
    public void registerReceiver(IReceiver listener) {
        if (debug) Log.i(TAG, "registerReceiver");
        this.listener = listener;
    }

//  private static class DebugWebSocket extends WebSocket {
    private class DebugWebSocket extends WebSocket {

        private DebugWebSocket(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            if (debug) Log.i(TAG, "onOpen");
            procState(EConnection.Opened);
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
            procState(EConnection.Closed);
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            byte[] receivedData = message.getBinaryPayload();
            if (debug) Log.i(TAG, String.format(Locale.US,
                    "onMessage received bytes: %d bytes, toString: %s",
                    receivedData.length, Arrays.toString(receivedData)));
            if (listener != null) {
                if (debug) Log.i(TAG, "onMessage redirecting");
                listener.onReceiveData(receivedData);
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
            if (debug) ServerCtrlNanoWebSocket.LOG.log(Level.SEVERE, "exception occured", exception);
            procState(EConnection.Failure);
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
