package by.citech.server.network.protocols.http.sockets;

import java.io.IOException;
import java.net.ServerSocket;

import by.citech.server.network.util.IFactoryThrowing;

/**
 * Creates a normal ServerSocket for TCP connections
 */
public class DefaultServerSocketFactory implements IFactoryThrowing<ServerSocket, IOException> {

    @Override
    public ServerSocket create() throws IOException {
        return new ServerSocket();
    }

}
