package by.citech.handsfree.debug;

import android.util.Log;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.logic.Caller;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IBaseAdder;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class Bt2BtRecorder
        implements IDebugListener, IBase {

    private final String TAG = Tags.BT2BT_RECORDER;
    private final boolean debug = Settings.debug;

    //--------------------- settings

    private int recordSize;
    private int btFactor;
    private int bt2btPacketSize;
    private byte[][] dataAssembled;
    private byte[][][] dataSaved;

    {
        takeSettings();
        applySettings();
    }

    private void applySettings() {
        dataAssembled = new byte[btFactor][bt2btPacketSize];
        dataSaved = new byte[recordSize][btFactor][bt2btPacketSize];
    }

    private void takeSettings() {
        recordSize = 100;
        btFactor = Settings.btFactor;
        bt2btPacketSize = Settings.bt2btPacketSize;
    }

    //--------------------- non-settings

    private int dataSavedCount;
    private StorageData<byte[]> storageFromBt;
    private StorageData<byte[][]> storageToBt;
    private boolean isPlaying;
    private boolean isRecording;
    private boolean isActive;

    public Bt2BtRecorder(StorageData<byte[]> storageFromBt, StorageData<byte[][]> storageToBt) {
        this.storageFromBt = storageFromBt;
        this.storageToBt = storageToBt;
    }

    @Override
    public void baseStart(IBaseAdder iBaseAdder) {
        if (debug) Log.i(TAG, "baseStart");
        if (iBaseAdder == null) {
            Log.e(TAG, "baseStart iBaseAdder is null");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
        isActive = true;
        new Thread(() -> {
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
            dataAssembled = null;
            dataSaved = null;
        }).start();
    }

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
            dataAssembled[dataAssembledCount] = storageFromBt.getData();
            dataAssembledCount++;
            if (dataAssembledCount == btFactor) {
                if (debug) Log.i(TAG, "run recorder output buffer contains enough data, saving");
                dataSaved[dataSavedCount] = dataAssembled;
                dataSavedCount++;
                if (debug) Log.i(TAG, String.format("run recorder cache contains %d arraysX2 of %d arraysX1 of %d bytes each", dataSavedCount, dataAssembledCount, Settings.bt2btPacketSize));
                dataAssembledCount = 0;
            }
        }
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        isActive = false;
    }

    private void play() {
        if (debug) Log.i(TAG, "play");
        for (int i = 0; i < dataSavedCount; i++) {
            storageToBt.putData(dataSaved[i]);
        }
        isPlaying = false;
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        switch (getCallerState()) {
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

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isPlaying = false;
        isRecording = false;
        storageToBt.clear();
    }

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

}
