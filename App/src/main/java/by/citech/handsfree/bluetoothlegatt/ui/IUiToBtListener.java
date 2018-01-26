package by.citech.handsfree.bluetoothlegatt.ui;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tretyak on 15.12.2017.
 */

public interface IUiToBtListener {
    void clickItemList(BluetoothDevice device);
    BluetoothDevice getConnectDevice();
    void clickBtnListener();

    boolean isScanning();
    boolean isConnecting();
}
