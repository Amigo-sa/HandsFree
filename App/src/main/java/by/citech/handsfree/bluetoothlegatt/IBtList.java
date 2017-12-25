package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;

public interface IBtList {
    void addDevice(BluetoothDevice device, int rssi);
    void clear();
}
