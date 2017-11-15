package by.citech.network.server.connection;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import by.citech.network.control.IReceiveListener;
import by.citech.network.control.IReceiveListenerReg;
import by.citech.network.control.ITransmitter;
import by.citech.param.Settings;
import by.citech.network.server.connection.protocols.http.IHTTPSession;
import by.citech.network.server.connection.websockets.CloseCode;
import by.citech.network.server.connection.websockets.NanoWSD;
import by.citech.network.server.connection.websockets.WebSocket;
import by.citech.network.server.connection.websockets.WebSocketFrame;
import by.citech.param.Messages;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

import static by.citech.util.Decode.bytesToHexMark1;

public class ServerCtrlNanoWebSocket extends NanoWSD implements IServerCtrl, IReceiveListenerReg, ITransmitter {
    private static final Logger LOG = Logger.getLogger(ServerCtrlNanoWebSocket.class.getName());
    private WebSocket webSocket;
    private Handler handler;
    private IReceiveListener listener;
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
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "sendMessage");
        try {
            webSocket.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendBytes(byte... bytes) {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "sendBytes");
        try {
            webSocket.send(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //--------------------- IExchangeCtrl

    @Override
    public ITransmitter getTransmitter() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "getTransmitter");
        return this;
    }

    @Override
    public IReceiveListenerReg getReceiverRegister() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "getReceiverRegister");
        return this;
    }

    //--------------------- IServerCtrl

    @Override
    public IServerCtrl startServer(int serverTimeout) throws IOException {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "startServer");
        start(serverTimeout);
        return this;
    }

    @Override
    public String getStatus () {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "getStatus");
        return this.status;
    }

    @Override
    public boolean isAliveServer() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "isAliveServer");
        return isAlive();
    }

    @Override
    public void stopServer() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "stopServer");
        stop();
    }

    //--------------------- IConnCtrl

    @Override
    public void closeConnection() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "closeConnection");
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
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "closeConnectionForce");
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
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "isAliveConnection");
        if (webSocket != null) {
            return webSocket.isOpen();
        }
        if (Settings.debug) Log.e(Tags.SRV_WSOCKETCTRL, "isAliveConnection webSocket is null");
        return false;
    }

    //--------------------- IReceiveListenerReg

    @Override
    public void registerReceiverListener(IReceiveListener listener) {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "registerReceiverListener");
        this.listener = listener;
    }

    //  private static class DebugWebSocket extends WebSocket {
    private class DebugWebSocket extends WebSocket {
        private DebugWebSocket(IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onOpen");
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
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onClose");
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL,
                    "Initiated by " + (initiatedByRemote ? "remote. " : "self. ") +
                    "Close code is <" + code + ">. " +
                    "Reason is <" + reason + ">.");

            status = StatusMessages.WEBSOCKET_CLOSED;
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onMessage");

            if (listener != null) {
                if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onMessage to redirect");
                listener.onReceiveMessage(message.getBinaryPayload());
            } else {
                if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onMessage to activity");
                handler.obtainMessage(StatusMessages.SRV_ONMESSAGE, message).sendToTarget();
            }
//          handler.obtainMessage(StatusMessages.SRV_ONMESSAGE, message.getTextPayload()).sendToTarget();
//          handler.sendEmptyMessage(StatusMessages.SRV_ONMESSAGE);
//          activity.textViewSrvFromCltText.setText(message.getTextPayload());
//          try {
//               message.setUnmasked();
//               sendFrame(message);
//          } catch (IOException e) {
//              throw new RuntimeException(e);
//          }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onPong");
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "Ponged: " + "<" + pong + ">");
            handler.sendEmptyMessage(StatusMessages.SRV_ONPONG);
        }

        @Override
        protected void onException(IOException exception) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "onException");
            if (Settings.debug) ServerCtrlNanoWebSocket.LOG.log(Level.SEVERE, "exception occured", exception);
            status = StatusMessages.WEBSOCKET_FAILURE;
            handler.sendEmptyMessage(StatusMessages.SRV_ONFAILURE);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "debugFrameReceived");
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, String.format("debugFrameReceived: <%s>", bytesToHexMark1(frame.getBinaryPayload())));
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "Received: " + "<" + frame + ">");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMERX);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "debugFrameSent");
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "Sended: " + "<" + frame + ">");
            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMETX);
        }
    }
}
