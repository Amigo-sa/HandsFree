package by.citech.logic;

public class ConnectorBluetooth {
    private static volatile ConnectorBluetooth instance = null;

    public static ConnectorBluetooth getInstance() {
        if (instance == null) {
            synchronized (ConnectorBluetooth.class) {
                if (instance == null) {
                    instance = new ConnectorBluetooth();
                }
            }
        }
        return instance;
    }
}
