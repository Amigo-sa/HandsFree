package by.citech.logic;

public class ConnectorAudio {

    //--------------------- singleton

    private static volatile ConnectorAudio instance = null;

    private ConnectorAudio() {
    }

    public static ConnectorAudio getInstance() {
        if (instance == null) {
            synchronized (ConnectorAudio.class) {
                if (instance == null) {
                    instance = new ConnectorAudio();
                }
            }
        }
        return instance;
    }

}
