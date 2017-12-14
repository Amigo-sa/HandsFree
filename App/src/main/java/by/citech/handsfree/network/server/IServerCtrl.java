package by.citech.handsfree.network.server;

import java.io.IOException;
import by.citech.handsfree.network.control.IConnCtrl;

public interface IServerCtrl
        extends IConnCtrl {
    IServerCtrl startServer(int serverTimeout) throws IOException;
    void stopServer();
    boolean isAliveServer();
    String getStatus();
}
