package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.Bt2AudOutLooper;
import by.citech.handsfree.debug.Bt2BtLooper;
import by.citech.handsfree.debug.Bt2BtRecorder;
import by.citech.handsfree.debug.AudIn2AudOutLooper;
import by.citech.handsfree.debug.ToBtLooper;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.DataSource;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

import static by.citech.handsfree.settings.enumeration.DataSource.DATAGENERATOR;
import static by.citech.handsfree.settings.enumeration.DataSource.MICROPHONE;

public class Caller
        implements IBase, ISettingsCtrl, IPrepareObject {

    private static final String STAG = Tags.Caller;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private INetInfoGetter iNetInfoGetter;
    private IBluetoothListener iBluetoothListener;
    private IBroadcastReceiver iBroadcastReceiver;
    private IService iService;
    private IBtToUiCtrl iBtToUiCtrl;
    private IMsgToUi iMsgToUi;
    private IBtList iBtList;
    private OpMode opMode;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return opMode != null;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        opMode = Settings.getInstance().getCommon().getOpMode();
        return true;
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
        } else {
            instance.prepareObject();
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

    public Caller setiService(IService iService) {
        this.iService = iService;
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

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        CallUi.getInstance().baseStart();
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
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        iNetInfoGetter = null;
        iBluetoothListener = null;
        opMode = null;
        iBroadcastReceiver = null;
        iService = null;
        iBtToUiCtrl = null;
        iMsgToUi = null;
        iBtList = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- data from bluetooth redirects to network and vice versa

    private void buildNormal() {
        if (debug) Log.i(TAG, "buildNormal");
        if (iNetInfoGetter == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            Log.e(TAG, "buildNormal illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.TO_BT_STORE);
        HandlerExtended handlerExtended = new HandlerExtended(ConnectorNet.getInstance());

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(handlerExtended)
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt)
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        ConnectorNet connectorNet = ConnectorNet.getInstance()
                .setStorageToNet(storageBtToNet)
                .setStorageFromNet(storageNetToBt)
                .setiNetInfoGetter(iNetInfoGetter)
                .setHandler(handlerExtended);

        connectorBluetooth.baseStart();
        connectorNet.baseStart();
    }

    //--------------------- data from data source redirects to bluetooth

    private void build2Bt(DataSource dataSource) {
        if (debug) Log.i(TAG, "build2Bt");
        if (iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null
                || dataSource == null) {
            if (debug) Log.e(TAG, "build2Bt illegal parameters");
            return;
        }

        StorageData<byte[][]> toBtStorage = new StorageData<>(Tags.TOBT_STORE);

        ToBtLooper toBtLooper = null;

        try {
            toBtLooper = new ToBtLooper(toBtStorage, dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setStorageToBt(toBtStorage)
                .setmHandler(new Handler())
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        connectorBluetooth.baseStart();

        if (toBtLooper != null) {
            toBtLooper.baseStart();
        }
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Log.i(TAG, "buildBt2AudOut");
        if (iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildBt2AudOut illegal parameters");
            return;
        }

        Bt2AudOutLooper bt2AudOutLooper = new Bt2AudOutLooper();

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .addIRxDataListener(bt2AudOutLooper)
                .setmHandler(new Handler())
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        connectorBluetooth.baseStart();
        bt2AudOutLooper.baseStart();
    }

    //--------------------- data from microphone redirects to dynamic

    private void buildAudIn2AudOut() {
        if (debug) Log.i(TAG, "buildAudIn2AudOut");

        AudIn2AudOutLooper audIn2AudOutLooper = new AudIn2AudOutLooper(true);
        audIn2AudOutLooper.baseStart();
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildBt2Bt() {
        if (debug) Log.i(TAG, "buildBt2Bt");
        if (iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageFromBt = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageToBt = new StorageData<>(Tags.TO_BT_STORE);

        Bt2BtLooper bt2BtLooper = new Bt2BtLooper(storageFromBt, storageToBt);

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageFromBt)
                .setStorageToBt(storageToBt)
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        connectorBluetooth.baseStart();
        bt2BtLooper.baseStart();
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildRecord() {
        if (debug) Log.i(TAG, "buildRecord");
        if (iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.TO_BT_STORE);

        Bt2BtRecorder bt2BtRecorder = new Bt2BtRecorder(storageBtToNet, storageNetToBt);

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt)
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);

        connectorBluetooth.baseStart();
        bt2BtRecorder.baseStart();
    }

    //--------------------- data from network looped back to network

    private void buildNet2Net() {
        if (debug) Log.i(TAG, "buildNet2Net");
    }

}
