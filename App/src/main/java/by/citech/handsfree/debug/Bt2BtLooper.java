package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class Bt2BtLooper
        implements IDebugCtrl, IBase {

    private static final String TAG = Tags.BT2BT_LOOPER;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private int btFactor;
    private int bt2btPacketSize;
    private byte[][] dataAssembled;

    {
        takeSettings();
        applySettings();
    }

    private void applySettings() {
        dataAssembled = new byte[btFactor][bt2btPacketSize];
    }

    private void takeSettings() {
        btFactor = Settings.btFactor;
        bt2btPacketSize = Settings.bt2btPacketSize;
    }

    //--------------------- non-settings

    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    private boolean isRunning;
    private boolean isActive;

    public Bt2BtLooper(StorageData<byte[]> storageBtToNet, StorageData<byte[][]> storageNetToBt) {
        this.storageBtToNet = storageBtToNet;
        this.storageNetToBt = storageNetToBt;
        isRunning = false;
        isActive = false;
    }

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
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
            dataAssembled = null;
            storageBtToNet = null;
            storageNetToBt = null;
        }).start();
        return true;
    }

    private void looping() {
        if (debug) Log.i(TAG, "looping");
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
            if (debug) Log.i(TAG, String.format("looping network output buffer contains %d arrays of %d bytes each", btCount, bt2btPacketSize));
            if (btCount == btFactor) {
                if (debug) Log.i(TAG, "looping network output buffer contains enough data, sending");
                btCount = 0;
                storageNetToBt.putData(dataAssembled);
            }
        }
    }

    @Override
    public boolean baseStop() {
//        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        isActive = false;
        return true;
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        storageBtToNet.setWriteLocked(false);
        storageNetToBt.setWriteLocked(false);
        isRunning = true;
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isRunning = false;
        storageBtToNet.setWriteLocked(true);
        storageNetToBt.setWriteLocked(true);
        storageBtToNet.clear();
        storageNetToBt.clear();
    }

}
