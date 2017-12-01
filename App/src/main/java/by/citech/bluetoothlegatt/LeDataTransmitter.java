package by.citech.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import by.citech.data.StorageData;
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

    //запускаем запись и нотификацию с устройства
    public void onlyReceiveData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
        mBluetoothLeService.initStore();
        if(characteristics.isEmpty()){
            mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
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



    private StorageData<byte[][]> storageNetToBt;
    private StorageData<byte[]> storageBtWrite;
    private boolean isRunning;
    private boolean isRunningWriteThread;
    private boolean Callback = true;
    private int lostWritePkt = 0;




    // Прослушка  калбэка onWriteCharacteristic
    @Override
    public void callbackIsDone() {
        Callback = true;
        if (Settings.debug) Log.i(TAG, "callbackIsDone()");
    }

    @Override
    public void rcvBtPktIsDone() {
        if (Settings.debug) Log.i(TAG, "rcvBtPktIsDone()");
    }



    //---------------------- Methods for write -------------------------

    private void writeThreadStart(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[][] arrayData = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
                int numBtPkt = 0;
                isRunning = true;
                while (isRunning){

                    if (!storageNetToBt.isEmpty() && numBtPkt == 0) {
                        // принимаем двоной массив данных из сети
                        arrayData = storageNetToBt.getData();
                    } else {
                        //разбираем двойной массив по BT пакетам
                        if (numBtPkt < Settings.btToNetFactor){
                            // запись данных производим с учётом Калбэка onWriteCharacteristic
                            if (Callback) {
                                writeByteArrayData(arrayData[numBtPkt]);
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
                            if(!Callback) {
                                Callback = true;
                                if (Settings.debug) Log.e(TAG, "num lost write package is " + lostWritePkt++);
                            }
                        } else
                            numBtPkt = 0;
                    }

                }
            }
        }).start();
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

    private void writeInCharacteristicByteArray(byte[] data, int writeType ){
        final BluetoothGattCharacteristic characteristic_write = characteristics.getWriteCharacteristic();
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        mBluetoothLeService.oneCharacteristicWrite(characteristic_write);
    }



}
