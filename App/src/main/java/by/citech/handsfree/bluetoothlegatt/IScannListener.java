package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tretyak on 14.12.2017.
 */

public interface IScannListener {
    void scanCallback(BluetoothDevice device, int rssi);
}
