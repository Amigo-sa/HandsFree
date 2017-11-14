package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.logic.Resource;
import by.citech.param.Settings;
import by.citech.param.Tags;

/**
 * Created by tretyak on 02.10.2017.
 */

public class WriterTransmitter extends Thread {

    private Resource res;
    private StorageData storageNetToBt;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private WriterTransmitterCallbackListener listener;
    private boolean isRunning;

    public WriterTransmitter(String name, Resource res, StorageData storageNetToBt, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        super(name);
        this.res = res;
        this.storageNetToBt = storageNetToBt;
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        isRunning = true;
        boolean timeOver = false;
        int timecounter = 0;
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        while (isRunning){
            if (!storageNetToBt.isEmpty() && (res.isCallback() || timeOver)) {
                if (Settings.debug) Log.i(Tags.BLE_WRITETRANS, "startClient storageNetToBt.getData()");
                characteristic.setValue(storageNetToBt.getData());
                mBluetoothGatt.writeCharacteristic(characteristic);
//                final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
//                for (byte byteChar : dataByte)
//                    stringBuilder.append(String.format("%02X ", byteChar));
                if (Settings.debug) Log.w(Tags.BLE_WRITETRANS, "Data write");
                res.setCallback(false);
                timeOver = false;
                timecounter = 0;
            }
            try {
                Thread.sleep(1);
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
