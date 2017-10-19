package by.citech.server.network;

import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.citech.param.Settings;
import by.citech.server.network.protocols.http.IHTTPSession;
import by.citech.server.network.websockets.CloseCode;
import by.citech.server.network.websockets.NanoWSD;
import by.citech.server.network.websockets.WebSocket;
import by.citech.server.network.websockets.WebSocketFrame;
import by.citech.param.Messages;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

import static by.citech.util.Decode.bytesToHex;

public class NanoWebSocketServerCtrl extends NanoWSD implements IServerCtrl {
    private static final Logger LOG = Logger.getLogger(NanoWebSocketServerCtrl.class.getName());
    private WebSocket webSocket;
    private Handler handler;
    private IServerListener listener;
    private String status = "";

    public NanoWebSocketServerCtrl(int port, Handler handler) {
        super(port);
        this.handler = handler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        webSocket = new DebugWebSocket(handshake);
        return webSocket;
    }

    //--------------------- IServerCtrl BEGIN

    @Override
    public void sendMessage(String message) {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "sendMessage");
        try {
            webSocket.send(message);
        } catch (IOException e) {
            Log.i(Tags.SRV_WSOCKETCTRL, "cant send message");
        }
    }

    @Override
    public void closeSocket() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "closeSocket");
        try {
            webSocket.close(CloseCode.NormalClosure, "Its all about me, DARLING", false);
        } catch (IOException e) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "closeSocket IOException");
        }
    }

    @Override
    public void setListener(IServerListener listener) {
        this.listener = listener;
    }

    @Override
    public WebSocket getWebSocket() {
        if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "getWebSocket");
        return webSocket;
    }

    @Override
    public String getStatus () {
        return this.status;
    }

    @Override
    public void startServer(int serverTimeout) throws IOException {
        start(serverTimeout);
    }

    @Override
    public boolean isAliveServer() {
        return isAlive();
    }

    @Override
    public void stopServer() {
        stop();
    }

    //--------------------- IServerCtrl END

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
                listener.onMessage(message.getBinaryPayload());
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
            if (Settings.debug) NanoWebSocketServerCtrl.LOG.log(Level.SEVERE, "exception occured", exception);
            status = StatusMessages.WEBSOCKET_FAILURE;
            handler.sendEmptyMessage(StatusMessages.SRV_ONEXCEPTION);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, "debugFrameReceived");
            if (Settings.debug) Log.i(Tags.SRV_WSOCKETCTRL, String.format("debugFrameReceived: <%s>", bytesToHex(frame.getBinaryPayload())));
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
