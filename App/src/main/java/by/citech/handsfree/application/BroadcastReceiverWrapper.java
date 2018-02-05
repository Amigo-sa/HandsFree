package by.citech.handsfree.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.LinkedList;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeCore;
import by.citech.handsfree.bluetoothlegatt.ConnectAction;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class BroadcastReceiverWrapper {

    private final static String TAG = Tags.BroadcastReceiverWrapper;

    private LinkedList<ConnectAction> listeners;

    public BroadcastReceiverWrapper() {
        listeners = new LinkedList<>();
    }

    public BroadcastReceiver getGattUpdateReceiver(){
        return mGattUpdateReceiver;
    }

    public void registerListener(ConnectAction connectAction){
        if (listeners != null || connectAction != null) {
            assert listeners != null;
            listeners.add(connectAction);
        }
    }

    public void clearListeners(){
        if (listeners != null)
            listeners.clear();
    }

    private void notifyConnectedListeners(){
        if (listeners != null)
            for (ConnectAction listener : listeners) {
                listener.actionConnected();
            }
    }

    private void notifyDisconnectedListeners(){
        if (listeners != null)
            for (ConnectAction listener : listeners) {
                listener.actionDisconnected();
            }
    }

    private void notifyServiceDiscovered(){
        if (listeners != null)
            for (ConnectAction listener : listeners) {
                listener.actionServiceDiscovered();
            }
    }

    private void notifyEnabled(){
        if (listeners != null)
            for (ConnectAction listener : listeners) {
                listener.actionDescriptorWrite();
            }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    // обьявляем обработчик(слушатель) соединения, для отображения состояния соединения на дисплее
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if (Settings.debug) Log.i(TAG, "onReceive");
            final String action = intent.getAction();
            if (BluetoothLeCore.ACTION_GATT_CONNECTED.equals(action)) {
                Timber.i("BluetoothLeCore.ACTION_GATT_CONNECTED");
                notifyConnectedListeners();
            } else if (BluetoothLeCore.ACTION_GATT_DISCONNECTED.equals(action)) {
                notifyDisconnectedListeners();
            } else if (BluetoothLeCore.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                notifyServiceDiscovered();
            } else if (BluetoothLeCore.ACTION_DESCRIPTOR_WRITE.equals(action)) {
                notifyEnabled();
            } else if (BluetoothLeCore.ACTION_DATA_AVAILABLE.equals(action)) {
                //if (Settings.debug) Log.i(TAG, "ACTION_DATA_AVAILABLE");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeCore.ACTION_DATA_WRITE.equals(action)){
                //if (Settings.debug) Log.i(TAG, "ACTION_DATA_WRITE");
                // displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
            }
        }
    };

    // определяем фильтр для нашего BroadcastReceivera, чтобы регистрировать конкретные события
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeCore.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeCore.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeCore.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeCore.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeCore.ACTION_DATA_WRITE);
        intentFilter.addAction(BluetoothLeCore.ACTION_DESCRIPTOR_WRITE);
        return intentFilter;
    }

}