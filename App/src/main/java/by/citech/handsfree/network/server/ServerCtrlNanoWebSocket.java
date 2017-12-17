package by.citech.handsfree.network.server;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    {objCount++;TAG = STAG + " " + objCount;}

    private WebSocket webSocket;
    private Handler handler;
    private IReceiver listener;
    private String status = "";

    public ServerCtrlNanoWebSocket(int port, Handler handler) {
        super(port);
        this.handler = handler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        webSocket = new DebugWebSocket(handshake);
        return webSocket;
    }

    //--------------------- ITransmitter

    @Override
    public void sendMessage(String message) {
        if (debug) Log.i(TAG, "sendMessage");
        try {
            webSocket.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendData(byte[] bytes) {
        if (debug) Log.i(TAG, "sendData");
        try {
            webSocket.send(bytes);
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
    public String getStatus () {
        if (debug) Log.i(TAG, "getStatus");
        return this.status;
    }

    @Override
    public boolean isAliveServer() {
        if (debug) Log.i(TAG, "isAliveServer");
        return isAlive();
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
        if (debug) Log.i(TAG, "isAliveConnection");
        if (webSocket != null) {
            return webSocket.isOpen();
        }
        if (debug) Log.e(TAG, "isAliveConnection webSocket is null");
        return false;
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
            status = StatusMessages.WEBSOCKET_OPENED;
            handler.sendEmptyMessage(StatusMessages.SRV_ONOPEN);
            try {
                send(Messages.SRV2CLT_ONOPEN);
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
            status = StatusMessages.WEBSOCKET_CLOSED;
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            if (debug) Log.i(TAG, "onMessage");
            if (listener != null) {
                if (debug) Log.i(TAG, "onMessage redirecting");
                listener.onReceiveData(message.getBinaryPayload());
            } else {
                if (debug) Log.i(TAG, "onMessage not redirecting");
                handler.obtainMessage(StatusMessages.SRV_ONMESSAGE, message).sendToTarget();
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (debug) Log.i(TAG, "onPong");
            if (debug) Log.i(TAG, "onPong " + pong);
            handler.sendEmptyMessage(StatusMessages.SRV_ONPONG);
        }

        @Override
        protected void onException(IOException exception) {
            if (debug) Log.i(TAG, "onException " + exception.getMessage());
            if (debug) ServerCtrlNanoWebSocket.LOG.log(Level.SEVERE, "exception occured", exception);
            status = StatusMessages.WEBSOCKET_FAILURE;
            handler.sendEmptyMessage(StatusMessages.SRV_ONFAILURE);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (debug) Log.i(TAG, "debugFrameReceived");
            if (debug) Log.i(TAG, "debugFrameReceived " + bytesToHexMark1(frame.getBinaryPayload()));
//          if (debug) Log.i(TAG, "debugFrameReceived " + frame);
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMERX);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (debug) Log.i(TAG, "debugFrameSent");
            if (debug) Log.i(TAG, "debugFrameSent " + bytesToHexMark1(frame.getBinaryPayload()));
//          if (debug) Log.i(TAG, "debugFrameSent " + frame);
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMETX);
        }

    }

}
