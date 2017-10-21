package by.citech.server.network;

import java.io.IOException;

import by.citech.connection.IReceiverListenerRegister;
import by.citech.connection.ITransmitter;
import by.citech.server.network.websockets.WebSocket;

public interface IServerCtrl {
    void startServer(int serverTimeout) throws IOException;
    void stopServer();
    void closeSocket();
    IReceiverListenerRegister getReceiverRegister();
    ITransmitter getTransmitter();
    boolean isAliveServer();
    WebSocket getWebSocket();
    String getStatus();
}
