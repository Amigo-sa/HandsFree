package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class Bt2BtLooper
        implements IDebugCtrl, IBase, ISettingsCtrl, IPrepareObject {

    private static final String STAG = Tags.BT2BT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private int btFactor;
    private int bt2btPacketSize;
    private byte[][] dataBuff;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    private boolean isRunning;
    private boolean isActive;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        applySettings(null);
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return dataBuff != null;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        dataBuff = new byte[btFactor][bt2btPacketSize];
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        btFactor = Settings.btFactor;
        bt2btPacketSize = Settings.bt2btPacketSize;
        return true;
    }

    //--------------------- non-settings

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
            dataBuff = null;
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
            dataBuff[btCount] = storageBtToNet.getData();
            btCount++;
            if (debug) Log.i(TAG, String.format("looping network output buffer contains %d arrays of %d bytes each", btCount, bt2btPacketSize));
            if (btCount == btFactor) {
                if (debug) Log.i(TAG, "looping network output buffer contains enough data, sending");
                btCount = 0;
                storageNetToBt.putData(dataBuff);
            }
        }
    }

    @Override
    public boolean baseStop() {
        IBase.super.baseStop();
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
