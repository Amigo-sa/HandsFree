package by.citech.handsfree.debug;

import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.debug.fsm.EDebugReport;
import by.citech.handsfree.debug.fsm.EDebugState;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

public class Bt2BtRecorder
        implements IThreading, IBuilding,
        DebugFsm.IDebugFsmListenerRegister,
        DebugFsm.IDebugFsmListener,
        DebugFsm.IDebugFsmReporter {

    private static final String STAG = Tags.Bt2BtRecorder;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

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
        Timber.tag(TAG).i("baseStart");
        registerDebugFsmListener(this, TAG);
        isActive = true;
        addRunnable(main);
    }

    @Override
    public void destroy() {
        Timber.tag(TAG).i("destroy");
        unregisterDebugFsmListener(this, TAG);
        stopDebug();
        isActive = false;
        dataBuff = null;
        dataSaved = null;
    }

    //--------------------- main

    private void record() {
        Timber.tag(TAG).i("record");
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
                Timber.tag(TAG).i("run recorder output buffer contains enough data, saving");
                dataSaved[dataSavedCount] = dataBuff;
                dataSavedCount++;
                Timber.tag(TAG).i(
                        "run recorder cache contains %d arraysX2 of %d arraysX1 of %d bytes each",
                        dataSavedCount, dataAssembledCount, Settings.Bluetooth.bt2BtPacketSize);
                dataAssembledCount = 0;
            }
        }
    }

    private void play() {
        Timber.tag(TAG).i("play");
        for (int i = 0; i < dataSavedCount; i++) {
            storageToBt.putData(dataSaved[i]);
        }
        isPlaying = false;
    }

    //--------------------- ICallFsmListener

    @Override
    public void onFsmStateChange(EDebugState from, EDebugState to, EDebugReport why) {
        Timber.tag(TAG).i("onFsmStateChange");
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
        Timber.tag(TAG).i("startDebug");
        switch (getDebugFsmState()) {
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
        Timber.tag(TAG).i("stopDebug");
        isPlaying = false;
        isRecording = false;
        storageToBt.clear();
    }

}
