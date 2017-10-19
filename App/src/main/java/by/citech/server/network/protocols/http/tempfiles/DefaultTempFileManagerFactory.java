package by.citech.server.network.protocols.http.tempfiles;

import by.citech.server.network.util.IFactory;

/**
 * Default strategy for creating and cleaning up temporary files.
 */
public class DefaultTempFileManagerFactory implements IFactory<ITempFileManager> {

    @Override
    public ITempFileManager create() {
        return new DefaultTempFileManager();
    }
}
