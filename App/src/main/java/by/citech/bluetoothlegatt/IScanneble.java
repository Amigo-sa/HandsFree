package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tretyak on 14.12.2017.
 */

public interface IScanneble {
    void scanCallback(BluetoothDevice device, int rssi);
}
