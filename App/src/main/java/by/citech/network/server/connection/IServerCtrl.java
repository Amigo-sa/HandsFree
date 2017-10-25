package by.citech.network.server.connection;

import java.io.IOException;
import by.citech.network.control.IConnCtrl;

public interface IServerCtrl extends IConnCtrl {
    IServerCtrl startServer(int serverTimeout) throws IOException;
    void stopServer();
    boolean isAliveServer();
    String getStatus();
}
