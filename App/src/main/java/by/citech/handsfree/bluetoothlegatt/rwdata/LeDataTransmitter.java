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
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.IThreadManager;
import by.citech.handsfree.traffic.INumberedTrafficAnalyzer;

public class LeDataTransmitter implements CallbackWriteListener, IThreadManager, INumberedTrafficAnalyzer {

    private final static String STAG = Tags.LeDataTransmitter;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final long WAIT_PERIOD = 5;//
    private static final long NOTIFY_SET_PERIOD = 200;//ms
    private static final int WRITE_TYPE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

    private BluetoothLeService mBluetoothLeService;
    private Characteristics characteristics;

    // обьявляем характеристику для включения нотификации на периферийном устройстве(сервере)
    private BluetoothGattCharacteristic characteristic_write;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private IBluetoothListener mIBluetoothListener;
    private volatile StorageData<byte[][]> storageToBt;
    private volatile StorageData<byte[]> storageFromBt;
    private boolean isRunning;
    private boolean isNotifyStopRunning;
    private boolean isNotifyStartRunning;
    private boolean callback = true;
    private boolean isMtuChanged = false;

    private int totalReceiveCount = 0;
    private ArrayList<ITransmitter> iRxDataListeners;

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

    //-----------------Observers method -----------------------------

    public void addIRxDataListener(ITransmitter iTransmitter) {
        iRxDataListeners.add(iTransmitter);
    }

    private void updateRxData(byte[] data){
        for (ITransmitter listener : iRxDataListeners) {
            listener.sendData(data);
        }
    }

    //----------------- Trasmit data -----------------------------

    //нотификацию с устройства
    public void onlyReceiveData() {
        if (debug) Log.i(TAG, "onlyReceiveData()");
        receiveDataStart();
    }

    public void enableTransmitData() {
        if (debug) Log.i(TAG, "enableTransmitData()");
        if (!isMtuChanged) setMTU();
        else               receiveDataStart();
    }

    //отключаем поток записи и нотификации
    public void disableTransmitData() {
        if (debug) Log.i(TAG, "disableTransmitData() writepkt = " + totalReceiveCount);
        writeThreadStop();
        notifyThreadStop();
        if (storageFromBt != null)
            storageFromBt.clear();
        if (storageToBt != null)
            storageToBt.clear();
        isMtuChanged = false;
    }

    //---------------------- Methods for notify -------------------------

    private boolean notifyCharacteristicStart() {
        return setCharacteristicNotification(true);
    }

    private boolean notifyCharacteristicStop() {
        return setCharacteristicNotification(false);
    }

    private void receiveDataStart() {
        if (!characteristics.isEmpty()) {
            notifyThreadStart();
        } else {
            if (debug) Log.i(TAG, "disconnectToast()");
            mIBluetoothListener.disconnectToast();
        }
    }

    private void notifyThreadStart() {
        addRunnable(() -> {
            int time = 0;
            isNotifyStartRunning = true;
            while (isNotifyStartRunning) {
                notifyCharacteristicStart();
                if (debug) Log.i(TAG, "DescriptorWriteAwait for start...");
                try {
                    Thread.sleep(NOTIFY_SET_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time++;
                if (time == WAIT_PERIOD) {
                    isNotifyStartRunning = false;
                    if (debug) Log.e(TAG, "Device not started notify");
                    if (mBluetoothLeService != null) {
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
            isNotifyStopRunning = true;
            while (isNotifyStopRunning) {
                notifyCharacteristicStop();
                if (debug) Log.i(TAG, "DescriptorWriteAwait for stop...");
                try {
                    Thread.sleep(NOTIFY_SET_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (time == WAIT_PERIOD) {
                    isNotifyStopRunning = false;
                    if (debug) Log.e(TAG, "Device not stop notify");
                }
                time++;
            }
        });
    }

    private boolean setCharacteristicNotification(boolean enable) {
        //if (debug) Log.i(TAG, "setNotifyCharacteristic = " + enable);
        mNotifyCharacteristic = characteristics.getNotifyCharacteristic();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, enable);
            //return callbackDescriptorWriteAwait();
            return true;
        }
        return false;
    }

    @Override
    public void callbackDescriptorIsDone() {
        if (debug) Log.i(TAG, "callbackDescriptorIsDone");
        //notifyDescriptorWritten = true;
        //после запуска нотификации запускаем и запись
        if (isNotifyStartRunning) {
            if (debug) Log.i(TAG, "notify started");
            characteristic_write = characteristics.getWriteCharacteristic();
            writeThreadStart();
            isNotifyStartRunning = false;
        }
        if (isNotifyStopRunning)
            if (debug) Log.i(TAG, "notify stopped");
        isNotifyStopRunning = false; //TODO: правильная логика?
    }

    @Override
    public void rcvBtPktIsDone(byte[] data) {
        //if (debug) Log.w(TAG, "notify: " + data.length);
        //if (debug) Log.i(TAG, "rcvBtPktIsDone()");
        analyzeNumberedBytes(data);
        totalReceiveCount++;
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
            case DataGen2Bt:
            default:
                break;
        }
    }

    //---------------------- Methods for write -------------------------

    private void setMTU(){
        mBluetoothLeService.requestMtu();
    }

    // Прослушка калбэка onMtuChanged
    @Override
    public void onMtuChangeIsDone(int mtu) {
        if (debug) Log.i(TAG, "onMtuChangeIsDone() = " + mtu);
        isMtuChanged = true;
        receiveDataStart();
    }

    private void writeThreadStart() {
        Log.w(TAG, String.format(Locale.US, "streamOn parameters is:" +
                        " btSinglePacket is %b," +
                        " btFactor is %d," +
                        " btLatencyMs is %d,",
                Settings.btSinglePacket,
                Settings.btFactor,
                Settings.btLatencyMs
        ));
        addRunnable(() -> {
            byte[][] arrayData;
            int numBtPkt;
            isRunning = true;
            totalReceiveCount = 0;
            resetStatistic();
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
                numBtPkt = 0;
                while (numBtPkt != arrayData.length) {
                    // запись данных производим с учётом Калбэка onWriteCharacteristic
                    if (callback) {
                        writeByteArrayData(arrayData[numBtPkt]);
                        callback = false;
                    }
                    // выдерживаем коннект интервал
                    try {
                        Thread.sleep(Settings.btLatencyMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //if (debug) Log.w(TAG, "numBtPkt = " + numBtPkt);
                    numBtPkt++;
                    // если Калбэк не пришёл то в любом случае разрешаем запись, поскольку коннет интервал был выдержан
                    if (!callback && (WRITE_TYPE == BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)) {
                        callback = true;
                        //if (debug) Log.e(TAG, "num write package without callback " + lostPackets++);
                    }
                }
            }
            resetStatistic();
        });
    }

    // Прослушка калбэка onWriteCharacteristic
    @Override
    public void callbackIsDone() {
        callback = true;
        //if (debug) Log.i(TAG, "callbackIsDone()");
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
//      if (debug) Log.w(TAG, "write: " + Arrays.toString(data));
        writeInCharacteristicByteArray(data, WRITE_TYPE);
    }

    private void writeInCharacteristicByteArray(byte[] data, int writeType) {
        characteristic_write.setValue(data);
        characteristic_write.setWriteType(writeType);
        if (mBluetoothLeService.oneCharacteristicWrite(characteristic_write)) {
            if (debug) Log.w(TAG, "write success");
        } else {
            if (debug) Log.w(TAG, "write fail");
        }
    }
}
