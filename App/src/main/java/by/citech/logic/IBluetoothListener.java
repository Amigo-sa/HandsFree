package by.citech.logic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import by.citech.bluetoothlegatt.adapters.LeDeviceListAdapter;


public interface IBluetoothListener {
    void changeOptionMenu();
    BluetoothManager getBluetoothManager();
    void addDeviceToList(LeDeviceListAdapter leDeviceListAdapter, final BluetoothDevice device, final int rssi);
    LeDeviceListAdapter addLeDeviceListAdapter();
    void finishConnection();
    void disconnectToast();
    void withoutDeviceView();
    void withDeviceView();
    String getUnknownServiceString();
    String unknownCharaString();
}
