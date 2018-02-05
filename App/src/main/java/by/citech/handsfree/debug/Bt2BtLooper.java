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

public class Bt2BtLooper
        implements IThreading, IBuilding,
        DebugFsm.IDebugFsmListenerRegister,
        DebugFsm.IDebugFsmListener,
        DebugFsm.IDebugFsmReporter {

    private static final String STAG = Tags.Bt2BtLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private int btFactor;
    private int bt2BtPacketSize;
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
        btFactor = Settings.Bluetooth.btFactor;
        bt2BtPacketSize = Settings.Bluetooth.bt2BtPacketSize;
    }

    //--------------------- non-settings

    public Bt2BtLooper(StorageData<byte[]> storageBtToNet, StorageData<byte[][]> storageNetToBt) {
        this.storageBtToNet = storageBtToNet;
        this.storageNetToBt = storageNetToBt;
        isRunning = false;
        isActive = false;
    }

    //--------------------- IBuilding

    @Override
    public void build() {
        Timber.tag(TAG).i("build");
        registerDebugFsmListener(this, TAG);
        isRunning = false;
        isActive = true;
        addRunnable(looping);
    }

    @Override
    public void destroy() {
        Timber.tag(TAG).i("destroy");
        unregisterDebugFsmListener(this, TAG);
        stopDebug();
        isActive = false;
        dataBuff = null;
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
        storageBtToNet.setWriteLocked(false);
        storageNetToBt.setWriteLocked(false);
        isRunning = true;
    }

    private void stopDebug() {
        Timber.tag(TAG).i("stopDebug");
        isRunning = false;
        storageBtToNet.setWriteLocked(true);
        storageNetToBt.setWriteLocked(true);
        storageBtToNet.clear();
        storageNetToBt.clear();
    }

    //--------------------- looping

    private void looping() {
        Timber.tag(TAG).i("looping");
        int btCount = 0;
        while (isRunning) {
            if (dataBuff == null) {
                dataBuff = new byte[btFactor][bt2BtPacketSize];
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
            Timber.tag(TAG).i("looping output buffer got array number %d, which have length of %d", btCount, dataBuff[btCount].length);
            btCount++;
            if (btCount == btFactor) {
                Timber.tag(TAG).i("looping output buffer contains enough data, putting in storage");
                btCount = 0;
                storageNetToBt.putData(dataBuff);
                dataBuff = null;
            }
        }
    }

}
