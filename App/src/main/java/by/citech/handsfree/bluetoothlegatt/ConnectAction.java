package by.citech.handsfree.bluetoothlegatt;

/**
 * Created by tretyak on 07.12.2017.
 */

public interface ConnectAction {
    void actionConnected();
    void actionDisconnected();
    void actionServiceDiscovered();
}
