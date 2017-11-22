package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeDataTransmitter {

    private final static String TAG = "WSD_LeDataTransmitter";

    private BluetoothLeService mBluetoothLeService;
    private Characteristics characteristics;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private IBluetoothListener mIBluetoothListener;

    public LeDataTransmitter(Characteristics characteristics,
                             IBluetoothListener mIBluetoothListener) {
        this.characteristics = characteristics;
        this.mIBluetoothListener = mIBluetoothListener;
    }

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    //запускаем запись и нотификацию с устройства
    public void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
        mBluetoothLeService.initStore();
        if(characteristics.isEmpty()){
            mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            final BluetoothGattCharacteristic characteristic_write = characteristics.getWriteCharacteristic();
            mBluetoothLeService.writeCharacteristic(characteristic_write);
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }
    //отключаем поток записи и нотификации
    public void disableTransmitData() {
        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
        if (mBluetoothLeService != null ){
            if( mBluetoothLeService.getWriteThread() != null){
                mBluetoothLeService.stopDataTransfer();
            }
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
        }
    }

}
