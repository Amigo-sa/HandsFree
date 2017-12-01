package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.ArrayList;

import by.citech.data.StorageData;
import by.citech.exchange.ITransmitter;
import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeDataTransmitter implements CallbackWriteListener{

    private final static String TAG = "WSD_LeDataTransmitter";

    private BluetoothLeService mBluetoothLeService;
    private Characteristics characteristics;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private IBluetoothListener mIBluetoothListener;

    private StorageData<byte[][]> storageToBt;
    private StorageData<byte[]> storageFromBt;
    private boolean isRunning;
    private boolean Callback = true;
    private int lostWritePkt = 0;
    private ArrayList<ITransmitter> iRxDataListeners;
    private BluetoothGattCharacteristic characteristic_write;

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

    //нотификацию с устройства
    public void onlyReceiveData() {
        if (Settings.debug) Log.i(TAG, "onlyReceiveData()");
        if(characteristics.isEmpty()){
            notifyCharacteristicStart();
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    public void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
        if(characteristics.isEmpty()){
            notifyCharacteristicStart();
            characteristic_write = characteristics.getWriteCharacteristic();
            writeThreadStart();
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    //отключаем поток записи и нотификации
    public void disableTransmitData() {
        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
        writeThreadStop();
        notifyCharacteristicStop();
    }

    //---------------------- Methods for notify -------------------------

    public void notifyCharacteristicStart() {
        if (Settings.debug) Log.i(TAG, "notifyCharacteristicStart()");
        mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
        if (mBluetoothLeService != null)
        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
    }

    public void notifyCharacteristicStop() {
        if(mBluetoothLeService != null)
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
    }

    @Override
    public void rcvBtPktIsDone(byte[] data) {
        if (Settings.debug) Log.i(TAG, "rcvBtPktIsDone()");
        if (storageFromBt != null)
            switch (Settings.debugMode) {
                case BtToAudio:
                    updateRxData(data);
                    break;
                case LoopbackBtToBt:
                case Normal:
                    storageFromBt.putData(data);
                    break;
                case MicToBt:
                default:
                    break;
            }
    }

    //---------------------- Methods for write -------------------------

    private void writeThreadStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[][] arrayData = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
                int numBtPkt = 0;
                isRunning = true;
                int pktSize = (Settings.singlePacket) ? Settings.btToMicFactor : Settings.btToNetFactor;
                while (isRunning) {

                    if (!storageToBt.isEmpty() && numBtPkt == 0) {
                        // принимаем двоной массив данных из сети
                        arrayData = storageToBt.getData();
                    } else {
                        //разбираем двойной массив по BT пакетам
                        if (numBtPkt < pktSize) {
                            // запись данных производим с учётом Калбэка onWriteCharacteristic
                            if (Callback) {
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

                            numBtPkt++;
                            // если Калбэк не пришёл то в любом случае разрешаем запись, поскольку коннет интервал был выдержан
                            if (!Callback) {
                                Callback = true;
                                if (Settings.debug)
                                    Log.e(TAG, "num lost write package is " + lostWritePkt++);
                            }
                        } else
                            numBtPkt = 0;
                    }
                }
            }
        }).start();
    }


//    if (!storageToBt.isEmpty() && Callback) {
//        writeByteArrayData(storageToBt.getData()[0]);
//        Callback = false;
//    }
//
//                        try {
//        Thread.sleep(Settings.btLatencyMs);
//    } catch (InterruptedException e) {
//        e.printStackTrace();
//    }
//
//    // если Калбэк не пришёл то в любом случае разрешаем запись, поскольку коннет интервал был выдержан
//                        if (!Callback) {
//        Callback = true;
//        if (Settings.debug)
//            Log.e(TAG, "num lost write package is " + lostWritePkt++);
//    }



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
        writeInCharacteristicByteArray(data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private void writeInCharacteristicByteArray(byte[] data, int writeType) {
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        mBluetoothLeService.oneCharacteristicWrite(characteristic_write);
    }
}
