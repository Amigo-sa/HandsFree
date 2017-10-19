package by.citech.server.network;

import java.io.IOException;
import by.citech.server.network.websockets.WebSocket;

public interface IServerCtrl {
    void sendMessage(String message);
    void closeSocket();
    void stopServer();
    void setListener(IServerListener listener);
    WebSocket getWebSocket();
    String getStatus();
    void startServer(int serverTimeout) throws IOException;
    boolean isAliveServer();
}
