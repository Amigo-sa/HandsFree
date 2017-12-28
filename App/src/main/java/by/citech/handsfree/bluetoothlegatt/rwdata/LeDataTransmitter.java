package by.citech.handsfree.bluetoothlegatt.rwdata;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.bluetoothlegatt.BluetoothLeService;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.logic.IBluetoothListener;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.IThreadManager;

/**
 * Created by tretyak on 22.11.2017.
 */

public class LeDataTransmitter implements CallbackWriteListener, IThreadManager {

    private final static String TAG = "WSD_LeDataTransmitter";
    private static final long WAIT_PERIOD = 5;//
    private static final long NOTIFY_SET_PERIOD = 200;//ms

    private BluetoothLeService mBluetoothLeService;
    private Characteristics characteristics;
    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic characteristic_write;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private IBluetoothListener mIBluetoothListener;
    private StorageData<byte[][]> storageToBt;
    private StorageData<byte[]> storageFromBt;
    private boolean isRunning;
    private boolean isNotyfyStopRunning;
    private boolean isNotyfyStartRunning;
    private boolean Callback = true;
    private int lostWritePkt = 0;
    private ArrayList<ITransmitter> iRxDataListeners;
    private boolean isMtuChanged = false;
    private boolean notifyDescriptorWritten = false;

    public LeDataTransmitter(Characteristics characteristics) {
        this.characteristics = characteristics;
        iRxDataListeners = new ArrayList<>();
    }

    //-------------------- setters -----------------------------

    public void setIBluetoothListener(IBluetoothListener mIBluetoothListener) {
        this.mIBluetoothListener = mIBluetoothListener;
    }
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
        if (!characteristics.isEmpty()) {
            notifyCharacteristicStart();
        } else {
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

//    public void enableTransmitData() {
//        if (Settings.debug) Log.i(TAG, "enableTransmitData()");
//
//        setMTU();
//        if (!characteristics.isEmpty()) {
//            if (notifyCharacteristicStart()) {
//                characteristic_write = characteristics.getWriteCharacteristic();
//                 writeThreadStart();
//            } else {
//                if (Settings.debug) Log.i(TAG, "CallbackDescriptorWrite was'nt receive");
//                ConnectorBluetooth.getInstance().processState();
//            }
//        } else {
//            if (Settings.debug) Log.i(TAG, "disconnectToast()");
//            mIBluetoothListener.disconnectToast();
//        }
//    }

    public void enableTransmitData() {
        if (Settings.debug) Log.i(TAG, "enableTransmitData()");

        setMTU();
        if (!characteristics.isEmpty()) {
            notifyThreadStart();
//            characteristic_write = characteristics.getWriteCharacteristic();
//            writeThreadStart();
        } else {
            if (Settings.debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }



//    //отключаем поток записи и нотификации
//    public void disableTransmitData() {
//        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
//        writeThreadStop();
//        if (!notifyCharacteristicStop()) {
//            if (Settings.debug) Log.i(TAG, "CallbackDescriptorWrite was'nt receive");
//        }
//    }

    //отключаем поток записи и нотификации
    public void disableTransmitData() {
        if (Settings.debug) Log.i(TAG, "disableTransmitData()");
        writeThreadStop();
        notifyThreadStop();
    }


    //---------------------- Methods for notify -------------------------

    private boolean notifyCharacteristicStart() {
        //notifyDescriptorWritten = false;
        return setCharacteristicNotification(true);
    }

       private boolean notifyCharacteristicStop() {
        //notifyDescriptorWritten = false;
        return setCharacteristicNotification(false);
    }


    private void notifyThreadStart() {
        addRunnable(() -> {
            int time = 0;
            isNotyfyStartRunning = true;
            while (isNotyfyStartRunning) {

                notifyCharacteristicStart();
                if (Settings.debug) Log.i(TAG, "DescriptorWriteAwait for start...");
                try {
                    Thread.sleep(NOTIFY_SET_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time++;
                if (time == WAIT_PERIOD) {
                    isNotyfyStartRunning = false;
                    if (Settings.debug) Log.e(TAG, "Device not started notify ");
                    if(mBluetoothLeService != null) {
                        //ConnectorBluetooth.getInstance().processState();
                        mBluetoothLeService.disconnect();
                    }
                }
            }
        });
    }

    private void notifyThreadStop() {
        addRunnable(() -> {
            int time = 0;
            isNotyfyStopRunning = true;
            while (isNotyfyStopRunning) {
                notifyCharacteristicStop();
                if (Settings.debug) Log.i(TAG, "DescriptorWriteAwait for stop...");
                try {
                    Thread.sleep(NOTIFY_SET_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (time == WAIT_PERIOD) {
                    isNotyfyStopRunning = false;
                    if (Settings.debug) Log.e(TAG, "Device not stop notify ");
                }
                time++;
            }
        });
    }

    private boolean setCharacteristicNotification(boolean enable){
        //if (Settings.debug) Log.i(TAG, "setNotifyCharacteristic = " + enable);
        mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
        if(mBluetoothLeService != null) {
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, enable);
            //return callbackDescriptorWriteAwait();
            return  true;
        }
        return false;
    }

//    private boolean callbackDescriptorWriteAwait() {
//        int time = 0;
//
//        while (!notifyDescriptorWritten) {
//            if (Settings.debug) Log.e(TAG, "DescriptorWriteAwait...");
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            time++;
//            if (time == WAIT_PERIOD)
//                return false;
//        }
//        return true;
//    }


    @Override
    public void callbackDescriptorIsDone() {
        if (Settings.debug) Log.i(TAG, "callbackDescriptorIsDone");
        //notifyDescriptorWritten = true;
        // после запуска нотификации запускаем и запись
        if (isNotyfyStartRunning) {
            if (Settings.debug) Log.i(TAG, "Notyfy Started");
            characteristic_write = characteristics.getWriteCharacteristic();
            writeThreadStart();
            isNotyfyStartRunning = false;
        }
        if (isNotyfyStopRunning)
            if (Settings.debug) Log.i(TAG, "Notyfy Stoped");
            isNotyfyStopRunning = false;
    }

    @Override
    public void rcvBtPktIsDone(byte[] data) {
        //if (Settings.debug) Log.i(TAG, "rcvBtPktIsDone()");
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
        addRunnable(() -> {
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
                    //if (Settings.debug) Log.w(TAG, "numBtPkt = " + numBtPkt);
                    numBtPkt++;
                    // если Калбэк не пришёл то в любом случае разрешаем запись, поскольку коннет интервал был выдержан
                    if (!Callback) {
                        Callback = true;
                        if (Settings.debug)
                            Log.e(TAG, "num write package without callback " + lostWritePkt++);
                    }
                }
                numBtPkt = 0;
            }
        });
    }
    // Прослушка  калбэка onWriteCharacteristic
    @Override
    public void callbackIsDone() {
        Callback = true;
        //if (Settings.debug) Log.i(TAG, "callbackIsDone()");
    }

    private void writeThreadStop(){
        isRunning = false;
    }

    /**
     * WRITE_TYPE_NO_RESPONSE -
     * WRITE_TYPE_DEFAULT -
     * WRITE_TYPE_SIGNED -
     * */
    private void writeByteArrayData(byte[] data){

        if (Settings.debug) Log.w(TAG, Arrays.toString(data));

        writeInCharacteristicByteArray(data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    private void writeInCharacteristicByteArray(byte[] data, int writeType) {
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        mBluetoothLeService.oneCharacteristicWrite(characteristic_write);
    }
}
