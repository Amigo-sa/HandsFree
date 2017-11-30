package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

import by.citech.exchange.ITransmitter;
import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 21.11.2017.
 */

public class LeBroadcastReceiver {

    private final static String TAG = "WSD_BroadcastReceiver";

    private boolean mConnected = false;
    private BluetoothDevice mBTDeviceConn, mBTDevice;

    private ControlAdapter controlAdapter;
    private LeScanner leScanner;
    private Characteristics characteristics;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ITransmitter> iRxDataListeners;

    private IBluetoothListener mIBluetoothListener;
    private StorageListener storageListener;//this

    public LeBroadcastReceiver(ControlAdapter controlAdapter,
                               LeScanner leScanner,
                               Characteristics characteristics,
                               IBluetoothListener mIBluetoothListener,
                               StorageListener storageListener) {

        this.controlAdapter = controlAdapter;
        this.leScanner = leScanner;
        this.characteristics = characteristics;
        this.mIBluetoothListener = mIBluetoothListener;
        this.storageListener = storageListener;
        iRxDataListeners = new ArrayList<>();
    }

    public void setBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public BluetoothDevice getmBTDeviceConn() {
        return mBTDeviceConn;
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
            if (Settings.debug) Log.i(TAG, "onReceive");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_CONNECTED");
                mConnected = true;
                mBTDeviceConn = mBTDevice;
                if (Settings.debug) Log.i(TAG, "mBTDevice = " + mBTDevice);
                if (mBTDevice != null)
                    mIBluetoothListener.connectDialogInfo(mBTDevice);
                controlAdapter.setBTDevice(mBTDeviceConn);
                controlAdapter.setConnected(mConnected);
                storageListener.setStorages();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mBTDeviceConn = null;
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_DISCONNECTED");
                if (mBTDevice != null)
                    mIBluetoothListener.disconnectDialogInfo(mBTDevice);
                controlAdapter.setBTDevice(mBTDeviceConn);
                controlAdapter.setConnected(mConnected);
                controlAdapter.clearAllDevicesFromList();
                //clearAllDevicesFromList();
                //leScanner.startScanBluetoothDevice();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                // Show all the supported services and characteristics on the user interface.
                characteristics.displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_AVAILABLE");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                if (Settings.debug) Log.i(TAG, intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA).toString());
                updateRxData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                if (Settings.debug) Log.i(TAG, "ACTION_DATA_WRITE");
                // displayWdata(intent.getStringExtra(BluetoothLeService.EXTRA_WDATA));
            }
        }
    };

    public void addIRxDataListener(ITransmitter iTransmitter) {
        iRxDataListeners.add(iTransmitter);
    }

    private void updateRxData(byte[] data){
        for (ITransmitter iRxDataListener : iRxDataListeners) {
            iRxDataListener.sendData(data);
        }
    }


    // Обновление данных LeBroadcastReceiver
    public void updateBroadcastReceiveData(){
        if (Settings.debug) Log.i(TAG, "updateBroadcastReceiveData()");
        mIBluetoothListener.registerIReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            if (mBTDevice != null) {
                final boolean result = mBluetoothLeService.connect(mBTDevice.getAddress());
                if (Settings.debug) Log.i(TAG, "Connect request result = " + result);
            }
        }
    }

    public void unregisterReceiver(){
        if (Settings.debug) Log.i(TAG, "unregisterReceiver()");
        mIBluetoothListener.unregisterIReceiver(mGattUpdateReceiver);
    }

    // определяем фильтр для нашего BroadcastReceivera, чтобы регистрировать конкретные события
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }

}