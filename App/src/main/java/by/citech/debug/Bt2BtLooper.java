package by.citech.debug;

import android.util.Log;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Bt2BtLooper
        implements IDebugListener, IDebugCtrl {

    private static final String TAG = Tags.BT2BT_LOOPER;
    private static final boolean debug = Settings.debug;

    private byte[][] dataAssembled;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    private boolean isRunning;
    private boolean isActive;

    public Bt2BtLooper(StorageData<byte[]> storageBtToNet, StorageData<byte[][]> storageNetToBt) {
        this.storageBtToNet = storageBtToNet;
        this.storageNetToBt = storageNetToBt;
        dataAssembled = new byte[Settings.bt2NetFactor][Settings.bt2btPacketSize];
        isRunning = false;
        isActive = false;
    }

    @Override
    public void activate() {
        //TODO: разве не должно быть по умолчанию isRunning=false, чтобы вызывать из интерфейса?
//      isRunning = true;
        isRunning = false;
        isActive = true;
        new Thread(() -> {
            while (isActive) {
                while (!isRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                looping();
            }
        }).start();
    }

    private void looping() {
        int btCount = 0;
        while (isRunning) {
            while (storageBtToNet.isEmpty()) {
                try {
                    Thread.sleep(5);
                    if (!isRunning) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dataAssembled[btCount] = storageBtToNet.getData();
            btCount++;
            if (debug) Log.i(TAG, String.format("run network output buffer contains %d arrays of %d bytes each", btCount, Settings.bt2btPacketSize));
            if (btCount == Settings.bt2NetFactor) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                btCount = 0;
                storageNetToBt.putData(dataAssembled);
            }
        }
    }

    @Override
    public void deactivate() {
        isActive = false;
        isRunning = false;
    }

    @Override
    public void startDebug() {
        isRunning = true;
    }

    @Override
    public void stopDebug() {
        isRunning = false;
    }

}
