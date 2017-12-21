package by.citech.handsfree.bluetoothlegatt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class LeBroadcastReceiver {

    private final static String TAG = "WSD_BroadcastReceiver";

    private ConnectAction connectAction;

    public LeBroadcastReceiver(ConnectAction connectAction) {
        this.connectAction = connectAction;
    }

    public BroadcastReceiver getGattUpdateReceiver(){
        return mGattUpdateReceiver;
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
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                connectAction.actionConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connectAction.actionDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                connectAction.actionServiceDiscovered();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_AVAILABLE");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_WRITE");
                // displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
            }
        }
    };

    // определяем фильтр для нашего BroadcastReceivera, чтобы регистрировать конкретные события
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }

}