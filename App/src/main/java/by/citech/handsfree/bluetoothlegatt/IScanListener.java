package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tretyak on 16.12.2017.
 */

public interface IScanListener {
    void onStartScan();
    void onStopScan();
    void scanCallback(BluetoothDevice device, int rssi);
}
