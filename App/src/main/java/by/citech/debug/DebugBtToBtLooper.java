package by.citech.debug;

import android.util.Log;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class DebugBtToBtLooper extends Thread implements IDebugListener {
    private static final String TAG = Tags.BT2BT_LOOPER;
    private static final boolean debug = Settings.debug;

    private byte[][] dataAssembled;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    private boolean isRunning;
    private boolean isActive;

    public DebugBtToBtLooper(StorageData<byte[]> storageBtToNet, StorageData<byte[][]> storageNetToBt) {
        this.storageBtToNet = storageBtToNet;
        this.storageNetToBt = storageNetToBt;
        dataAssembled = new byte[Settings.btToNetFactor][Settings.btToBtSendSize];
        isRunning = false;
        isActive = false;
    }

    @Override
    public void run() {
        isActive = true;
        isRunning = true;
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
            if (debug) Log.i(TAG, String.format("run network output buffer contains %d arrays of %d bytes each", btCount, Settings.btToBtSendSize));
            if (btCount == Settings.btToNetFactor) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                btCount = 0;
                storageNetToBt.putData(dataAssembled);
            }
        }
    }

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
