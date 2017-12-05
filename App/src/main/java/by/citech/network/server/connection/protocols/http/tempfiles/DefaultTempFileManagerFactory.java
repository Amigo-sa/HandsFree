package by.citech.network.server.connection.protocols.http.tempfiles;

import by.citech.network.server.connection.util.IFactory;

/**
 * SettingsDefault strategy for creating and cleaning up temporary files.
 */
public class DefaultTempFileManagerFactory implements IFactory<ITempFileManager> {

    @Override
    public ITempFileManager create() {
        return new DefaultTempFileManager();
    }
}
