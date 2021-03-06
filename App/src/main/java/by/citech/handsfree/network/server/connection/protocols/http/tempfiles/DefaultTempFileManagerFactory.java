package by.citech.handsfree.network.server.connection.protocols.http.tempfiles;

import by.citech.handsfree.network.server.connection.util.IFactory;

/**
 * Default strategy for creating and cleaning up temporary files.
 */
public class DefaultTempFileManagerFactory implements IFactory<ITempFileManager> {

    @Override
    public ITempFileManager create() {
        return new DefaultTempFileManager();
    }
}
