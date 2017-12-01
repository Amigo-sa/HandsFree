package  by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.debug.ITrafficUpdate;
import by.citech.logic.Resource;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class WriteCharacteristicThread extends Thread implements ITrafficUpdate, CallbackWriteListener {

    public static final String TAG = "WRS_WRT";

    private Resource res;
    private StorageData<byte[][]> storageToBt;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private WriterTransmitterCallbackListener listener;
    private boolean isRunning;
    private boolean Callback = true;
    private boolean Notify = false;
    private int callbackCnt = 0;
    private int rcvCnt = 0;
    private byte[][] arrayData = new byte[Settings.toBtFactor][Settings.btToBtSendSize];
    private long prevTime = 0L;
    private long deltaTime = 0L;

    public WriteCharacteristicThread(String name, Resource res, StorageData<byte[][]> storageToBt, BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic) {
        super(name);
        this.res = res;
        this.storageToBt = storageToBt;
        this.mBluetoothGatt = mBluetoothGatt;
        this.characteristic = characteristic;
    }

    public void addWriteListener(WriterTransmitterCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        int numBTpackage = 0;
        byte[] dataWrite;
        boolean isArrayDataEmpty = true;
        isRunning = true;
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        while (isRunning) {
            if (!Settings.singlePacket) {
                if ((!isArrayDataEmpty || !storageToBt.isEmpty()) && (Callback || Notify)) {
                    if (Settings.debug)
                        Log.i(Tags.BLE_WRITETRANS, "startClient storageToBt.getData()");
                    if (isArrayDataEmpty) {
                        arrayData = storageToBt.getData();
                        isArrayDataEmpty = false;
                    }
                    if (numBTpackage < Settings.toBtFactor) {
                        dataWrite = arrayData[numBTpackage];
                        //if (Settings.debug) Log.w(Tags.BLE_WRITETRANS,"from dataWrite " + Decode.bytesToHexMark1(dataWrite));
                        numBTpackage++;
                        if (!Settings.debug && (numBTpackage == Settings.toBtFactor)) {
                            deltaTime = System.currentTimeMillis() - prevTime;
                            Log.i(TAG, "getFromArray latency = " + deltaTime);
                        }
                        characteristic.setValue(dataWrite);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                        if (Settings.debug)
                            Log.w(Tags.BLE_WRITETRANS, "Data write numBTpackage = " + numBTpackage);
                    } else {
                        numBTpackage = 0;
                        isArrayDataEmpty = true;
                    }
                    if (Settings.debug) Log.i(TAG, "writeCharacteristic() ");
                    Callback = false;
                    Notify = false;
                }
                if (numBTpackage < Settings.toBtFactor)
                    try {
                        Thread.sleep(Settings.btLatencyMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            } else {
                if (!storageToBt.isEmpty() && (Callback || Notify)) {
                    characteristic.setValue(storageToBt.getData()[0]);
                    mBluetoothGatt.writeCharacteristic(characteristic);
                    Callback = false;
                    Notify = false;
                }
                try {
                    Thread.sleep(Settings.btLatencyMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (listener != null)
            listener.doWriteCharacteristic("Write Characteristic ended");
    }


    public void cancel() {
        isRunning = false;
    }

    @Override
    public void callbackIsDone() {
        Callback = true;
        if (Settings.debug) Log.i(TAG, "callbackIsDone() " + callbackCnt++ );
    }

    @Override
    public void rcvBtPktIsDone() {
        Notify = true;
        if (Settings.debug) Log.i(TAG, "rcvBtPktIsDone() " + rcvCnt++);
    }

    //--------------------- debug

    @Override
    public long getBytesDelta() {
        return 0;
    }

}
