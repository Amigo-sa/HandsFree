package by.citech.handsfree.bluetoothlegatt.rwdata;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.settings.Settings;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeDataTransmitter implements CallbackWriteListener {

    private final static String TAG = "WSD_LeDataTransmitter";
    private static final long WAIT_PERIOD = 50;

    private BluetoothLeService mBluetoothLeService;
    private Characteristics characteristics;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic characteristic_write;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private IBluetoothListener mIBluetoothListener;
    private StorageData<byte[][]> storageToBt;
    private StorageData<byte[]> storageFromBt;
    private boolean isRunning;
    private boolean Callback = true;
    private int lostWritePkt = 0;
    private ArrayList<ITransmitter> iRxDataListeners;
    private boolean isMtuChanged = false;
    private boolean notifyDescriptorWritten = false;

    public LeDataTransmitter(Characteristics characteristics,
                             IBluetoothListener mIBluetoothListener) {
        this.characteristics = characteristics;
        this.mIBluetoothListener = mIBluetoothListener;
        iRxDataListeners = new ArrayList<>();
    }

    //-------------------- setters -----------------------------

    public void setBluetoothLeService(BluetoothLeService mBluetoothLeService) {
        this.mBluetoothLeService = mBluetoothLeService;
    }

    public void setStorageToBt(StorageData<byte[][]> storageToBt) {
        this.storageToBt = storageToBt;
    }

    public void setStorageFromBt(StorageData<byte[]> storageFromBt) {
        this.storageFromBt = storageFromBt;
    }

    //-----------------Oservers method -----------------------------

    public void addIRxDataListener(ITransmitter iTransmitter) {
        iRxDataListeners.add(iTransmitter);
    }

    private void updateRxData(byte[] data){
        for (ITransmitter iRxDataListener : iRxDataListeners) {
            iRxDataListener.sendData(data);
        }
    }

    //-----------------Trasmit data -----------------------------

    //нотификацию с устройства
    public void onlyReceiveData() {
        if (Settings.debug) Log.i(TAG, "onlyReceiveData()");
        if(!characteristics.isEmpty()){
            notifyCharacteristicStart();
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    public void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");

        setMTU();
        if(!characteristics.isEmpty()){
            if (notifyCharacteristicStart()) {  
                characteristic_write = characteristics.getWriteCharacteristic();
                 writeThreadStart();
             } else
                   if (Settings.debug) Log.i(TAG, "CallbackDescriptorWrite was'nt receive");
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    //отключаем поток записи и нотификации
    public void disableTransmitData() {
        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
        writeThreadStop();
        if (!notifyCharacteristicStop())
            if (Settings.debug) Log.i(TAG, "CallbackDescriptorWrite was'nt receive");
    }

    //---------------------- Methods for notify -------------------------

    private boolean notifyCharacteristicStart() {
        notifyDescriptorWritten = false;
        return setCharacteristicNotification(true);
    }

       private boolean notifyCharacteristicStop() {
        notifyDescriptorWritten = false;
        return setCharacteristicNotification(false);
    }

    private boolean setCharacteristicNotification(boolean enable){
        //if (Settings.debug) Log.i(TAG, "setNotifyCharacteristic = " + enable);
        mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
        if(mBluetoothLeService != null) {
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, enable);
            return callbackDescriptorWriteAwait();
        }
        return false;
    }
    private boolean callbackDescriptorWriteAwait() {
        int time = 0;

        while (!notifyDescriptorWritten) {
            if (Settings.debug) Log.e(TAG, "DescriptorWriteAwait...");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            if (time == WAIT_PERIOD)
                return false;
        }
        return true;
    }
    @Override
    public void callbackDescriptorIsDone() {
        if (Settings.debug) Log.i(TAG, "callbackDescriptorIsDone");
        notifyDescriptorWritten = true;
    }

    @Override
    public void rcvBtPktIsDone(byte[] data) {
        if (Settings.debug) Log.i(TAG, "rcvBtPktIsDone()");
        switch (Settings.opMode) {
            case Bt2AudOut:
                updateRxData(data);
                break;
            case Bt2Bt:
            case Normal:
                if (storageFromBt != null) {
                    storageFromBt.putData(data);
                }
                break;
            case AudIn2Bt:
            default:
                break;
        }
    }

    //---------------------- Methods for write -------------------------

    private void setMTU(){
        if (!isMtuChanged) {
            mBluetoothLeService.requestMtu();
            if (!awaitMTUChange())
                if (Settings.debug) Log.e(TAG, "callback from requestMtu was'nt get");
        }
    }

    private boolean awaitMTUChange(){
        int time = 0;

        while (!isMtuChanged) {
            if (Settings.debug) Log.e(TAG, "awaitMTUChange...");
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            if (time == WAIT_PERIOD)
                return false;
        }
        return true;


    }

    // Прослушка  калбэка onMtuChanged
    @Override
    public void MtuChangedDone(int mtu) {
        if (Settings.debug) Log.i(TAG, "MtuChangedDone() = " + mtu);
        isMtuChanged = true;
    }

    private void writeThreadStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[][] arrayData;
                int numBtPkt = 0;
                isRunning = true;
                int pktSize = (Settings.btSinglePacket) ? 1 : Settings.btFactor;
                while (isRunning) {
                    while (storageToBt.isEmpty()) {
                        try {
                            Thread.sleep(5);
                            if (!isRunning) return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    arrayData = storageToBt.getData();
                    while (numBtPkt != pktSize) {
                        // запись данных производим с учётом Калбэка onWriteCharacteristic
                        if (Callback) {
                            if (Settings.debug) Log.w(TAG, "writeByteArrayData()");
                            writeByteArrayData(arrayData[numBtPkt]);
                            //if (Settings.debug) Log.e(TAG, "numBtPkt = " + numBtPkt);
                            Callback = false;
                        }
                        // выдерживаем коннект интервал
                        try {
                            Thread.sleep(Settings.btLatencyMs);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (Settings.debug) Log.w(TAG, "numBtPkt = " + numBtPkt);
                        numBtPkt++;
                        // если Калбэк не пришёл то в любом случае разрешаем запись, поскольку коннет интервал был выдержан
                        if (!Callback) {
                            Callback = true;
                            if (Settings.debug)
                                Log.e(TAG, "num lost write package is " + lostWritePkt++);
                        }
                    }
                    numBtPkt = 0;
                }
            }
        }).start();
    }
    // Прослушка  калбэка onWriteCharacteristic
    @Override
    public void callbackIsDone() {
        Callback = true;
        if (Settings.debug) Log.i(TAG, "callbackIsDone()");
    }

    private void writeThreadStop(){
        isRunning = false;
    }

    /**
     * WRITE_TYPE_NO_RESPONSE -
     * WRITE_TYPE_DEFAULT -
     * WRITE_TYPE_SIGNED -
     * */
    public void writeByteArrayData(byte[] data){

        if (Settings.debug) Log.w(TAG, Arrays.toString(data));

        writeInCharacteristicByteArray(data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private void writeInCharacteristicByteArray(byte[] data, int writeType) {
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        mBluetoothLeService.oneCharacteristicWrite(characteristic_write);
    }
}