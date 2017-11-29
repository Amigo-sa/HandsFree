package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

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
    private LeBroadcastReceiver leBroadcastReceiver;
    private IBluetoothListener mIBluetoothListener;

    public LeConnector(LeScanner leScanner,
                       LeBroadcastReceiver leBroadcastReceiver,
                       IBluetoothListener mIBluetoothListener) {
        this.leScanner = leScanner;
        this.leBroadcastReceiver = leBroadcastReceiver;
        this.mIBluetoothListener = mIBluetoothListener;
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
        // останавливаем сканирование
        leScanner.stopScanBluetoothDevice();
        // если сервис привязан производим соединение
        if (mBluetoothLeService != null)
            mBluetoothLeService.connect(mDeviceAddress);
        leBroadcastReceiver.setBluetoothLeService(mBluetoothLeService);
        // ответ ждём в Callback(LeBroadcastReceiver)
    }

}