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

public class Bt2BtRecorder
        implements IBase, ISettingsCtrl, IPrepareObject, IThreadManager,
        ICallerFsmRegisterListener, ICallerFsmListener, ICallerFsm {

    private static final String STAG = Tags.Bt2BtRecorder;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private int recordSize;
    private int btFactor;
    private int bt2BtPacketSize;
    private byte[][] dataBuff;
    private byte[][][] dataSaved;
    private int dataSavedCount;
    private StorageData<byte[]> storageFromBt;
    private StorageData<byte[][]> storageToBt;
    private boolean isPlaying;
    private boolean isRecording;
    private boolean isActive;

    private Runnable main = new Runnable() {
        @Override
        public void run() {
            while (isActive) {
                while (!isPlaying && !isRecording) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isPlaying) {
                    play();
                }
                if (isRecording) {
                    record();
                    storageFromBt.setWriteLocked(true);
                }
            }
            storageFromBt = null;
            storageToBt = null;
            dataBuff = null;
            dataSaved = null;
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
        return dataBuff != null && dataSaved != null;
    }

    @Override
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        dataBuff = new byte[btFactor][bt2BtPacketSize];
        dataSaved = new byte[recordSize][btFactor][bt2BtPacketSize];
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        recordSize = 100;
        btFactor = Settings.btFactor;
        bt2BtPacketSize = Settings.bt2BtPacketSize;
        return true;
    }

    //--------------------- constructor

    public Bt2BtRecorder(StorageData<byte[]> storageFromBt, StorageData<byte[][]> storageToBt) {
        this.storageFromBt = storageFromBt;
        this.storageToBt = storageToBt;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        prepareObject();
        registerCallerFsmListener(this, TAG);
        isActive = true;
        addRunnable(main);
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        unregisterCallerFsmListener(this, TAG);
        stopDebug();
        isActive = false;
        dataBuff = null;
        dataSaved = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- main

    private void record() {
        if (debug) Log.i(TAG, "record");
        int dataAssembledCount = 0;
        while (isRecording) {
            while (storageFromBt.isEmpty()) {
                try {
                    Thread.sleep(5);
                    if (!isRecording) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dataBuff[dataAssembledCount] = storageFromBt.getData();
            dataAssembledCount++;
            if (dataAssembledCount == btFactor) {
                if (debug) Log.i(TAG, "run recorder output buffer contains enough data, saving");
                dataSaved[dataSavedCount] = dataBuff;
                dataSavedCount++;
                if (debug) Log.i(TAG, String.format("run recorder cache contains %d arraysX2 of %d arraysX1 of %d bytes each", dataSavedCount, dataAssembledCount, Settings.bt2BtPacketSize));
                dataAssembledCount = 0;
            }
        }
    }

    private void play() {
        if (debug) Log.i(TAG, "play");
        for (int i = 0; i < dataSavedCount; i++) {
            storageToBt.putData(dataSaved[i]);
        }
        isPlaying = false;
    }

    //--------------------- ICallerFsmListener

    @Override
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
        switch (getCallerFsmState()) {
            case DebugPlay:
                isPlaying = true;
                break;
            case DebugRecord:
                isRecording = true;
                break;
            default:
                break;
        }
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isPlaying = false;
        isRecording = false;
        storageToBt.clear();
    }

}
