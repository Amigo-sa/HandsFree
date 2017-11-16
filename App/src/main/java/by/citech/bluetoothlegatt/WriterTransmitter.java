package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.Arrays;

import by.citech.data.StorageData;
import by.citech.logic.Resource;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.util.Decode;

/**
 * Created by tretyak on 02.10.2017.
 */

public class WriterTransmitter extends Thread {

    private Resource res;
    private StorageData<byte[][]> storageNetToBt;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private WriterTransmitterCallbackListener listener;
    private boolean isRunning;
    private byte[][] arrayData = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];

    public WriterTransmitter(String name, Resource res, StorageData<byte[][]> storageNetToBt, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        super(name);
        this.res = res;
        this.storageNetToBt = storageNetToBt;
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    private byte[] getBTpackage(int numBTPackage) {

//        byte[] singleBTPackage = new byte[Settings.btToBtSendSize];
//        for (int i = 0; i < Settings.btToBtSendSize; i++) {
//            singleBTPackage[i] = arrayData[i + (numBTPackage * Settings.btToBtSendSize)];
//        }

        return arrayData[numBTPackage];
    }



    @Override
    public void run() {
        int numBTpackage = 0;
        byte[] dataWrite;
        boolean isArrayDataEmpty = true;
        isRunning = true;
        boolean timeOver = false;
        int timecounter = 0;
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        while (isRunning){
            if ( (!isArrayDataEmpty || !storageNetToBt.isEmpty()) && (res.isCallback() || timeOver)) {
                if (Settings.debug) Log.i(Tags.BLE_WRITETRANS, "startClient storageNetToBt.getData()");
                if(isArrayDataEmpty) {
                    arrayData = storageNetToBt.getData();
                    isArrayDataEmpty = false;
                }
                if (numBTpackage < Settings.btToNetFactor) {
                    dataWrite = getBTpackage(numBTpackage);
                    if (Settings.debug) Log.w(Tags.BLE_WRITETRANS,"from dataWrite " + Decode.bytesToHexMark1(dataWrite));
                    numBTpackage++;
                    characteristic.setValue(dataWrite);
                    mBluetoothGatt.writeCharacteristic(characteristic);
                    if (Settings.debug) Log.w(Tags.BLE_WRITETRANS, "Data write numBTpackage = " + numBTpackage);
                }else{
                    numBTpackage = 0;
                    isArrayDataEmpty = true;
                }

//                final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
//                for (byte byteChar : dataByte)
//                    stringBuilder.append(String.format("%02X ", byteChar));
                if (Settings.debug) Log.w(Tags.BLE_WRITETRANS, "before set callback");
                res.setCallback(false);
                timeOver = false;
                timecounter = 0;
            }
            try {
                Thread.sleep(8);
                if (!res.isCallback()) {
                    timecounter++;
                }

                if (timecounter == 10) {
                    timeOver = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        if (listener != null)
            listener.doWriteCharacteristic("");
    }

    public void cancel() {
        isRunning = false;
    }
}
