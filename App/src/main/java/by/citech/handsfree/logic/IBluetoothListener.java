package by.citech.handsfree.logic;

import android.bluetooth.BluetoothManager;

import by.citech.handsfree.bluetoothlegatt.adapters.LeDeviceListAdapter;


public interface IBluetoothListener {
    void changeOptionMenu();
    BluetoothManager getBluetoothManager();
    void finishConnection();
    void disconnectToast();
    void withoutDeviceView();
    void withDeviceView();
    String getUnknownServiceString();
    String unknownCharaString();
}
