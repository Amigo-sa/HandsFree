package by.citech.bluetoothlegatt.rwdata;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.ArrayList;

import by.citech.bluetoothlegatt.BluetoothLeService;
import by.citech.data.StorageData;
import by.citech.exchange.ITransmitter;
import by.citech.logic.IBluetoothListener;
import by.citech.param.Settings;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeDataTransmitter implements CallbackWriteListener {

    private final static String TAG = "WSD_LeDataTransmitter";

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
        if(characteristics.isEmpty()){
            notifyCharacteristicStart();
        } else{
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    public void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
        mBluetoothLeService.requestMtu();
        if(characteristics.isEmpty()){
            while (!isMtuChanged){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
        writeInCharacteristicByteArray(data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private void writeInCharacteristicByteArray(byte[] data, int writeType) {
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        mBluetoothLeService.oneCharacteristicWrite(characteristic_write);
    }
}
