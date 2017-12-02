package by.citech.debug;

import android.util.Log;
import by.citech.data.StorageData;
import by.citech.logic.Caller;
import by.citech.logic.CallerState;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Bt2BtRecorder
        implements IDebugListener, IDebugCtrl {

    private final String TAG;
    private final boolean debug;

    private final int recordSize;
    private final int toBtFactor;
    private final int bt2btPacketSize;

    {
        TAG = Tags.BT2BT_RECORDER;
        debug = Settings.debug;

        recordSize = 100;
        toBtFactor = Settings.btFactor;
        bt2btPacketSize = Settings.bt2btPacketSize;
    }

    private byte[][] dataAssembled;
    private int dataSavedCount;
    private byte[][][] dataSaved;
    private StorageData<byte[]> storageBtToNet;
    private StorageData<byte[][]> storageNetToBt;
    private boolean isPlaying;
    private boolean isRecording;
    private boolean isActive;

    public Bt2BtRecorder(StorageData<byte[]> storageBtToNet, StorageData<byte[][]> storageNetToBt) {
        this.storageBtToNet = storageBtToNet;
        this.storageNetToBt = storageNetToBt;
        dataAssembled = new byte[toBtFactor][bt2btPacketSize];
        dataSaved = new byte[recordSize][toBtFactor][bt2btPacketSize];
        isPlaying = false;
        isRecording = false;
        isActive = false;
        dataSavedCount = 0;
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
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
                    storageBtToNet.setWriteLocked(true);
                }
            }
        }).start();
    }

    private void record() {
        if (debug) Log.i(TAG, "record");
        int dataAssembledCount = 0;
        while (isRecording) {
            while (storageBtToNet.isEmpty()) {
                try {
                    Thread.sleep(5);
                    if (!isRecording) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dataAssembled[dataAssembledCount] = storageBtToNet.getData();
            dataAssembledCount++;
            if (dataAssembledCount == Settings.bt2NetFactor) {
                if (debug) Log.i(TAG, "run recorder output buffer contains enough data, saving");
                dataSaved[dataSavedCount] = dataAssembled;
                dataSavedCount++;
                if (debug) Log.i(TAG, String.format("run recorder cache contains %d arraysX2 of %d arraysX1 of %d bytes each", dataSavedCount, dataAssembledCount, Settings.bt2btPacketSize));
                dataAssembledCount = 0;
            }
        }
    }

    @Override
    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isActive = false;
        isPlaying = false;
        isRecording = false;
    }

    private void play() {
        if (debug) Log.i(TAG, "play");
        for (int i = 0; i < dataSavedCount; i++) {
            storageNetToBt.putData(dataSaved[i]);
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
        storageNetToBt.clear();
    }

    private String getCallerStateName() {
        return Caller.getInstance().getCallerState().getName();
    }

    private CallerState getCallerState() {
        return Caller.getInstance().getCallerState();
    }

}
