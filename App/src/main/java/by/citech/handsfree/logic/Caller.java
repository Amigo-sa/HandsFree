package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.loopers.Bt2AudOutLooper;
import by.citech.handsfree.loopers.Bt2BtLooper;
import by.citech.handsfree.loopers.Bt2BtRecorder;
import by.citech.handsfree.loopers.AudIn2AudOutLooper;
import by.citech.handsfree.loopers.ToBtLooper;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

import static by.citech.handsfree.settings.EDataSource.DATAGENERATOR;
import static by.citech.handsfree.settings.EDataSource.MICROPHONE;

public class Caller {

    private static final String STAG = Tags.Caller;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private INetInfoGetter iNetInfoGetter;
    private IBluetoothListener iBluetoothListener;
    private IBroadcastReceiver iBroadcastReceiver;
    private IBtToUiCtrl iBtToUiCtrl;
    private IMsgToUi iMsgToUi;
    private IBtList iBtList;
    private EOpMode opMode;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        opMode = Settings.Common.opMode;
    }

    //--------------------- singleton

    private static volatile Caller instance = null;

    private Caller() {
    }

    public static Caller getInstance() {
        if (instance == null) {
            synchronized (Caller.class) {
                if (instance == null) {
                    instance = new Caller();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public Caller setiNetInfoGetter(INetInfoGetter listener) {
        iNetInfoGetter = listener;
        return this;
    }

    public Caller setiBluetoothListener(IBluetoothListener listener) {
        iBluetoothListener = listener;
        return this;
    }

    public Caller setiBroadcastReceiver(IBroadcastReceiver iBroadcastReceiver) {
        this.iBroadcastReceiver = iBroadcastReceiver;
        return this;
    }

    public Caller setiBtToUiCtrl(IBtToUiCtrl iBtToUiCtrl) {
        this.iBtToUiCtrl = iBtToUiCtrl;
        return this;
    }

    public Caller setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public Caller setiBtList(IBtList iBtList) {
        this.iBtList = iBtList;
        return this;
    }

    //--------------------- main

    public void build() {
        if (debug) Timber.i("build");
        switch (opMode) {
            case Bt2Bt:
                buildBt2Bt();
                break;
            case Net2Net:
                buildNet2Net();
                break;
            case Record:
                buildRecord();
                break;
            case Bt2AudOut:
                buildBt2AudOut();
                break;
            case AudIn2AudOut:
                buildAudIn2AudOut();
                break;
            case AudIn2Bt:
                build2Bt(MICROPHONE);
                break;
            case DataGen2Bt:
                build2Bt(DATAGENERATOR);
                break;
            case Normal:
            default:
                buildNormal();
                break;
        }
    }

    public void destroy() {
        if (debug) Timber.i("destroy");
        iNetInfoGetter = null;
        iBluetoothListener = null;
        opMode = null;
        iBroadcastReceiver = null;
        iBtToUiCtrl = null;
        iMsgToUi = null;
        iBtList = null;
    }

    //--------------------- data from bluetooth redirects to network and vice versa

    private void buildNormal() {
        if (debug) Timber.i("buildNormal");
        if (iNetInfoGetter == null
                || iBluetoothListener == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Timber.e("buildNormal illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.TO_BT_STORE);
        HandlerExtended handlerExtended = new HandlerExtended(ConnectorNet.getInstance());

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(handlerExtended)
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        ConnectorNet.getInstance()
                .setStorageToNet(storageBtToNet)
                .setStorageFromNet(storageNetToBt)
                .setiNetInfoGetter(iNetInfoGetter)
                .setHandler(handlerExtended);
    }

    //--------------------- data from data source redirects to bluetooth

    private void build2Bt(EDataSource dataSource) {
        if (debug) Timber.i("build2Bt");
        if (iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null
                || dataSource == null) {
            if (debug) Timber.e("build2Bt illegal parameters");
            return;
        }

        StorageData<byte[][]> toBtStorage = new StorageData<>(Tags.TO_BT_STORE);

        ToBtLooper toBtLooper = null;

        try {
            toBtLooper = new ToBtLooper(toBtStorage, dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setStorageToBt(toBtStorage)
                .setmHandler(new Handler())
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        if (toBtLooper != null) {
            toBtLooper.build();
        }
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Timber.i("buildBt2AudOut");
        if (iBluetoothListener == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Timber.e("buildBt2AudOut illegal parameters");
            return;
        }

        Bt2AudOutLooper bt2AudOutLooper = new Bt2AudOutLooper();

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .addIRxDataListener(bt2AudOutLooper)
                .setmHandler(new Handler())
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        bt2AudOutLooper.build();
    }

    //--------------------- data from microphone redirects to dynamic

    private void buildAudIn2AudOut() {
        if (debug) Timber.i("buildAudIn2AudOut");

        AudIn2AudOutLooper audIn2AudOutLooper = new AudIn2AudOutLooper(true);
        audIn2AudOutLooper.build();
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildBt2Bt() {
        if (debug) Timber.i("buildBt2Bt");
        if (iBluetoothListener == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Timber.e("buildBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageFromBt = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageToBt = new StorageData<>(Tags.TO_BT_STORE);

        Bt2BtLooper bt2BtLooper = new Bt2BtLooper(storageFromBt, storageToBt);

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageFromBt)
                .setStorageToBt(storageToBt)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        bt2BtLooper.build();
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildRecord() {
        if (debug) Timber.i("buildRecord");
        if (iBluetoothListener == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Timber.e("buildBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.TO_BT_STORE);

        Bt2BtRecorder bt2BtRecorder = new Bt2BtRecorder(storageBtToNet, storageNetToBt);

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        bt2BtRecorder.build();
    }

    //--------------------- data from network looped back to network

    private void buildNet2Net() {
        if (debug) Timber.i("buildNet2Net");
    }

}
