package by.citech.websocketduplex.client.network;

import okhttp3.WebSocket;
import okio.ByteString;

public interface IOkWebSocketClientListener {
    void onMessage(ByteString bytes);
    void onMessage(String text);
    void onOpen(WebSocket webSocket);
    void onClosing (int code, String reason);
    void onClosed(int code, String reason);
    void onFailure();
}
