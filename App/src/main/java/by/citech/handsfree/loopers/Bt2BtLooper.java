package by.citech.handsfree.loopers;

import android.util.Log;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.management.IBase;
import by.citech.handsfree.logic.ECallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegisterListener;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ESeverityLevel;
import by.citech.handsfree.threading.IThreadManager;

public class Bt2BtLooper
        implements IBase, ISettingsCtrl, IPrepareObject, IThreadManager,
        ICallerFsm, ICallerFsmRegisterListener, ICallerFsmListener {

    private static final String STAG = Tags.Bt2BtLooper;
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

    private Runnable looping = new Runnable() {
        @Override
        public void run() {
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
        }
    };

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
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
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

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        prepareObject();
        isRunning = false;
        isActive = true;
        addRunnable(looping);
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        unregisterCallerFsmListener(this, TAG);
        stopDebug();
        isActive = false;
        dataBuff = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- ICallerFsmListener

    public void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case StartDebug:
                startDebug();
                break;
            case StopDebug:
                stopDebug();
                break;
            default:
                break;
        }
    }

    private void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        storageBtToNet.setWriteLocked(false);
        storageNetToBt.setWriteLocked(false);
        isRunning = true;
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isRunning = false;
        storageBtToNet.setWriteLocked(true);
        storageNetToBt.setWriteLocked(true);
        storageBtToNet.clear();
        storageNetToBt.clear();
    }

    //--------------------- looping

    private void looping() {
        if (debug) Log.i(TAG, "looping");
        int btCount = 0;
        while (isRunning) {
            if (dataBuff == null) {
                dataBuff = new byte[btFactor][bt2btPacketSize];
            }
            while (storageBtToNet.isEmpty()) {
                try {
                    Thread.sleep(5);
                    if (!isRunning) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dataBuff[btCount] = storageBtToNet.getData();
            if (debug) Log.i(TAG, String.format("looping output buffer got array number %d, which have length of %d", btCount, dataBuff[btCount].length));
            btCount++;
            if (btCount == btFactor) {
                if (debug) Log.i(TAG, "looping output buffer contains enough data, putting in storage");
                btCount = 0;
                storageNetToBt.putData(dataBuff);
                dataBuff = null;
            }
        }
    }

}
