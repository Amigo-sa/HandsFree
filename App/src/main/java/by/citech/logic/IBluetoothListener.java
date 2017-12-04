package by.citech.logic;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import by.citech.bluetoothlegatt.adapters.LeDeviceListAdapter;


public interface IBluetoothListener {
    void changeOptionMenu();
    void addDeviceToList(LeDeviceListAdapter leDeviceListAdapter, final BluetoothDevice device, final int rssi);
    LeDeviceListAdapter addLeDeviceListAdapter();
    void finishConnection();
    void disconnectToast();
    void disconnectDialogInfo(BluetoothDevice bluetoothDevice);
    void connectDialogInfo(BluetoothDevice bluetoothDevice);
    void registerIReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter);
    void unregisterIReceiver(BroadcastReceiver broadcastReceiver);
    String getUnknownServiceString();
    String unknownCharaString();
}
