package by.citech.server.network;

import java.io.IOException;

import by.citech.connection.IReceiver;
import by.citech.connection.IReceiverRegister;
import by.citech.connection.ITransmitter;
import by.citech.server.network.websockets.WebSocket;

public interface IServerCtrl {
    void startServer(int serverTimeout) throws IOException;
    void stopServer();
    void closeSocket();
    IReceiverRegister getReceiverRegister();
    ITransmitter getTransmitter();
    boolean isAliveServer();
    WebSocket getWebSocket();
    String getStatus();
}
