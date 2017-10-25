package by.citech.server.network;

import java.io.IOException;
import by.citech.connection.IConnCtrl;

public interface IServerCtrl extends IConnCtrl {
    IServerCtrl startServer(int serverTimeout) throws IOException;
    void stopServer();
    boolean isAliveServer();
    String getStatus();
}
