package by.citech.websocketduplex.server.network;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import by.citech.websocketduplex.server.network.protocols.http.IHTTPSession;
import by.citech.websocketduplex.server.network.websockets.CloseCode;
import by.citech.websocketduplex.server.network.websockets.NanoWSD;
import by.citech.websocketduplex.server.network.websockets.WebSocket;
import by.citech.websocketduplex.server.network.websockets.WebSocketFrame;
import by.citech.websocketduplex.util.Messages;
import by.citech.websocketduplex.util.StatusMessages;
import by.citech.websocketduplex.util.Tags;

public class NanoWebSocketServerCtrl extends NanoWSD {
    private static final Logger LOG = Logger.getLogger(NanoWebSocketServerCtrl.class.getName());
    private final boolean debug;
    private WebSocket webSocket;
    private Handler handler;
    private String status = "";

    public NanoWebSocketServerCtrl(int port, boolean debug, Handler handler) {
        super(port);
        this.debug = debug;
        this.handler = handler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        webSocket = new DebugWebSocket(this, handshake);
        return webSocket;
    }

    public void sendMessage(String message) {
        if (debug) {
            Log.i(Tags.SRV_WSOCKETCTRL, "sendMessage");
        }

        try {
            webSocket.send(message);
        } catch (IOException e) {
            Log.i(Tags.SRV_WSOCKETCTRL, "cant send message");
        }
    }

    public void closeSocket() {
        if (debug) {
            Log.i(Tags.SRV_WSOCKETCTRL, "closeSocket");
        }

        try {
            webSocket.close(CloseCode.NormalClosure, "Its all about me, DARLING", false);
        } catch (IOException e) {
            Log.i(Tags.SRV_WSOCKETCTRL, "closeSocket IOException");
        }
    }

    public WebSocket getWebSocket() {
        if (debug) {
            Log.i(Tags.SRV_WSOCKETCTRL, "getWebSocket");
        }

        return webSocket;
    }

    public String getStatus () {
        return this.status;
    }

//  private static class DebugWebSocket extends WebSocket {
    private class DebugWebSocket extends WebSocket {
        private final NanoWebSocketServerCtrl server;

        public DebugWebSocket(NanoWebSocketServerCtrl server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "onOpen");
            }

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
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "onClose");
                Log.i(Tags.SRV_WSOCKETCTRL, "Initiated by " + (initiatedByRemote ? "remote. " : "self. ")
                        + "Close code is " + (code != null ? code : "unknown") + ". "
                        + (reason != null && !reason.isEmpty() ? "Reason is: <" + reason + ">" : ""));
            }

            status = StatusMessages.WEBSOCKET_CLOSED;
            handler.sendEmptyMessage(StatusMessages.SRV_ONCLOSE);
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "onMessage");
            }

            handler.obtainMessage(StatusMessages.SRV_ONMESSAGE, message.getTextPayload()).sendToTarget();
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
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "onPong");
                Log.i(Tags.SRV_WSOCKETCTRL, "Ponged: " + "<" + pong + ">");
            }

            handler.sendEmptyMessage(StatusMessages.SRV_ONPONG);
        }

        @Override
        protected void onException(IOException exception) {
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "onException");
                NanoWebSocketServerCtrl.LOG.log(Level.SEVERE, "exception occured", exception);
            }

            status = StatusMessages.WEBSOCKET_FAILURE;
            handler.sendEmptyMessage(StatusMessages.SRV_ONEXCEPTION);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "debugFrameReceived");
                Log.i(Tags.SRV_WSOCKETCTRL, "Received: " + "<" + frame + ">");
            }

            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMERX);
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (debug) {
                Log.i(Tags.SRV_WSOCKETCTRL, "debugFrameSent");
                Log.i(Tags.SRV_WSOCKETCTRL, "Sended: " + "<" + frame + ">");
            }

            handler.sendEmptyMessage(StatusMessages.SRV_ONDEBUGFRAMETX);
        }
    }
}
