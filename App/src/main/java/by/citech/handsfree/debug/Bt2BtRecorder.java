package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.threading.IThreading;

public class Bt2BtRecorder
        implements IThreading, IBuilding,
        CallFsm.ICallFsmListenerRegister, CallFsm.ICallFsmListener, CallFsm.ICallFsmReporter {

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
        recordSize = 100;
        btFactor = Settings.Bluetooth.btFactor;
        bt2BtPacketSize = Settings.Bluetooth.bt2BtPacketSize;
        dataBuff = new byte[btFactor][bt2BtPacketSize];
        dataSaved = new byte[recordSize][btFactor][bt2BtPacketSize];
    }

    //--------------------- constructor

    public Bt2BtRecorder(StorageData<byte[]> storageFromBt, StorageData<byte[][]> storageToBt) {
        this.storageFromBt = storageFromBt;
        this.storageToBt = storageToBt;
    }

    //--------------------- IBuilding

    @Override
    public void build() {
        if (debug) Log.i(TAG, "baseStart");
        registerCallFsmListener(this, TAG);
        isActive = true;
        addRunnable(main);
    }

    @Override
    public void destroy() {
        if (debug) Log.i(TAG, "destroy");
        unregisterCallFsmListener(this, TAG);
        stopDebug();
        isActive = false;
        dataBuff = null;
        dataSaved = null;
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
                if (debug) Log.i(TAG, String.format("run recorder cache contains %d arraysX2 of %d arraysX1 of %d bytes each", dataSavedCount, dataAssembledCount, Settings.Bluetooth.bt2BtPacketSize));
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

    //--------------------- ICallFsmListener

    @Override
    public void onCallerStateChange(ECallState from, ECallState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case RP_StartDebug:
                startDebug();
                break;
            case RP_StopDebug:
                stopDebug();
                break;
            default:
                break;
        }
    }

    private void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        switch (getCallFsmState()) {
            case ST_DebugPlay:
                isPlaying = true;
                break;
            case ST_DebugRecord:
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
