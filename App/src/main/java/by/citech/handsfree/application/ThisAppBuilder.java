package by.citech.handsfree.application;

import android.os.Handler;

import by.citech.handsfree.activity.fsm.ActivityFsm;
import by.citech.handsfree.bluetoothlegatt.ConnectorBluetooth;
import by.citech.handsfree.bluetoothlegatt.IBluetoothListener;
import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.bluetoothlegatt.fsm.BtFsm;
import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.network.ConnectorNet;
import by.citech.handsfree.common.HandlerExtended;
import by.citech.handsfree.network.fsm.NetFsm;
import by.citech.handsfree.ui.IBtToUiCtrl;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.Bt2AudOutLooper;
import by.citech.handsfree.debug.Bt2BtLooper;
import by.citech.handsfree.debug.Bt2BtRecorder;
import by.citech.handsfree.debug.AudIn2AudOutLooper;
import by.citech.handsfree.debug.ToBtLooper;
import by.citech.handsfree.ui.IMsgToUi;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.bluetoothlegatt.IScanListener;
import timber.log.Timber;

import static by.citech.handsfree.settings.EDataSource.DATAGENERATOR;
import static by.citech.handsfree.settings.EDataSource.MICROPHONE;

public class ThisAppBuilder implements
        CallFsm.ICallFsmListenerRegister,
        BtFsm.IBtFsmListenerRegister,
        NetFsm.INetFsmListenerRegister,
        ActivityFsm.IActivityFsmListenerRegister,
        DebugFsm.IDebugFsmListenerRegister {

    private static final String TAG = Tags.ThisAppBuilder;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private INetInfoGetter iNetInfoGetter;
    private IBluetoothListener iBluetoothListener;
    private IScanListener iScanListener;
    private IBtToUiCtrl iBtToUiCtrl;
    private IMsgToUi iMsgToUi;
    private IBtList iBtList;
    private EOpMode opMode;

    {
        opMode = Settings.Common.opMode;
    }

    //--------------------- singleton

    private static volatile ThisAppBuilder instance = null;

    public ThisAppBuilder(EOpMode opMode) {
        this.opMode = opMode;
    }

    //--------------------- getters and setters

    public ThisAppBuilder setiNetInfoGetter(INetInfoGetter listener) {
        iNetInfoGetter = listener;
        return this;
    }

    public ThisAppBuilder setiBluetoothListener(IBluetoothListener listener) {
        iBluetoothListener = listener;
        return this;
    }

    public ThisAppBuilder setiScanListener(IScanListener iScanListener) {
        this.iScanListener = iScanListener;
        return this;
    }

    public ThisAppBuilder setiBtToUiCtrl(IBtToUiCtrl iBtToUiCtrl) {
        this.iBtToUiCtrl = iBtToUiCtrl;
        return this;
    }

    public ThisAppBuilder setiMsgToUi(IMsgToUi iMsgToUi) {
        this.iMsgToUi = iMsgToUi;
        return this;
    }

    public ThisAppBuilder setiBtList(IBtList iBtList) {
        this.iBtList = iBtList;
        return this;
    }

    //--------------------- main

    public void build() {
        if (debug) Timber.i("build");
        switch (opMode) {
            case Bt2Bt:        buildBt2Bt(); break;
            case Net2Net:      buildNet2Net(); break;
            case Record:       buildRecord(); break;
            case Bt2AudOut:    buildBt2AudOut(); break;
            case AudIn2AudOut: buildAudIn2AudOut(); break;
            case AudIn2Bt:     build2Bt(MICROPHONE); break;
            case DataGen2Bt:   build2Bt(DATAGENERATOR); break;
            case Normal:
            default:           buildNormal(); break;
        }
    }

    public void destroy() {
        if (debug) Timber.i("destroy");
        iNetInfoGetter = null;
        iBluetoothListener = null;
        opMode = null;
        iBtToUiCtrl = null;
        iMsgToUi = null;
        iBtList = null;
    }

    //--------------------- data from bluetooth redirects to network and vice versa

    private void buildNormal() {
        if (debug) Timber.i("buildNormal");

        if (iNetInfoGetter == null
                || iBluetoothListener == null
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
                .setiScanListener(iScanListener, true);

        ConnectorNet.getInstance()
                .setStorageToNet(storageBtToNet)
                .setStorageFromNet(storageNetToBt)
                .setiNetInfoGetter(iNetInfoGetter)
                .setHandler(handlerExtended);

        registerCallFsmListener(ThisApp.getCallControl(), Tags.CallControl);
    }

    //--------------------- data from data source redirects to bluetooth

    private void build2Bt(EDataSource dataSource) {
        if (debug) Timber.i("build2Bt");
        if (       iBtToUiCtrl == null
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
                .setiScanListener(iScanListener, true);

        if (toBtLooper != null) {
            toBtLooper.build();
        }
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Timber.i("buildBt2AudOut");
        if (iBluetoothListener == null
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
                .setiScanListener(iScanListener, true);

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
                .setiScanListener(iScanListener, true);

        bt2BtLooper.build();
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildRecord() {
        if (debug) Timber.i("buildRecord");
        if (iBluetoothListener == null
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
                .setiScanListener(iScanListener, true);

        bt2BtRecorder.build();
    }

    //--------------------- data from network looped back to network

    private void buildNet2Net() {
        if (debug) Timber.i("buildNet2Net");
    }

}
