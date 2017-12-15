package by.citech.handsfree.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.handsfree.param.Settings;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeConnector {

    private final static String TAG = "WSD_LeConnector";

    // выводим на дисплей принимаемые данные
    private String mDeviceAddress;
    private BluetoothDevice mBTDevice;
    private LeScanner leScanner;
    private BluetoothLeService mBluetoothLeService;

    public LeConnector(LeScanner leScanner) {
        this.leScanner = leScanner;
    }

    public void setBTDevice(BluetoothDevice mBTDevice) {
        this.mBTDevice = mBTDevice;
    }

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    public void disconnectBTDevice(){
        // производим отключение от устройства
        if (Settings.debug) Log.i(TAG,"disconnectBTDevice()");
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
        }
    }

    public void onConnectBTDevice() {
        if (Settings.debug) Log.i(TAG,"onConnectBTDevice()");
        // получаем данные от присоединяемого устройсва
        mDeviceAddress = mBTDevice.getAddress();
        // если сервис привязан производим соединение
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(mDeviceAddress);
    }
}