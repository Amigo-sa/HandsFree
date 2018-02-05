package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;

public interface IBtList {
    void addDevice(BluetoothDevice device, boolean connecting, boolean connected);
    void removeDevice(BluetoothDevice device);
    void clear();
}
