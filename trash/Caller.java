package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.bluetoothlegatt.IBtList;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.debug.IDebugCtrl;
import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.Bt2AudOutLooper;
import by.citech.handsfree.debug.Bt2BtLooper;
import by.citech.handsfree.debug.Bt2BtRecorder;
import by.citech.handsfree.debug.AudIn2AudOutLooper;
import by.citech.handsfree.debug.AudIn2BtLooper;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

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

    private volatile CallerState callerState;
    private ICallToUiListener iCallToUiListener;
    private ICallNetListener iCallNetListener;
    private INetInfoGetter iNetInfoGetter;
    private IBluetoothListener iBluetoothListener;
    private IDebugCtrl iDebugCtrl;
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
        callerState = CallerState.PhaseZero;
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return callerState != null && opMode != null;
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

    public Caller setiCallToUiListener(ICallToUiListener listener) {
        iCallToUiListener = listener;
        return this;
    }

    public Caller setiCallNetListener(ICallNetListener listener) {
        iCallNetListener = listener;
        return this;
    }

    public Caller setiNetInfoGetter(INetInfoGetter listener) {
        iNetInfoGetter = listener;
        return this;
    }

    public Caller setiBluetoothListener(IBluetoothListener listener) {
        iBluetoothListener = listener;
        return this;
    }

    public Caller setiDebugCtrl(IDebugCtrl listener) {
        this.iDebugCtrl = listener;
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

    //--------------------- work with fsm

    synchronized CallerState getCallerState() {
        if (debug) Log.i(TAG, "getCallerState is " + callerState.getName());
        return callerState;
    }

    synchronized boolean setState(CallerState fromCallerState, CallerState toCallerState) {
        if (debug) Log.w(TAG, String.format("setState from %s to %s", fromCallerState.getName(), toCallerState.getName()));
        if (callerState == fromCallerState) {
            if (fromCallerState.availableStates().contains(toCallerState)) {
                callerState = toCallerState;
                if (callerState == CallerState.Error) {
                    callerState = CallerState.Idle;  // TODO: обработку ошибок? ожидание отклика?
                } else if (callerState == CallerState.Idle) {
                    callerState = CallerState.Idle;  // TODO: переводить не в Idle, а PhaseZero и ожидать готовность?
                }
                return true;
            } else {
                if (debug) Log.e(TAG, String.format("setState: %s is not available from %s", toCallerState.getName(), fromCallerState.getName()));
            }
        } else {
            if (debug) Log.e(TAG, String.format("setState: current is not %s", fromCallerState.getName()));
        }
        return false;
    }

    //--------------------- main

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        prepareObject();
        if (iCallToUiListener == null) {
            if (debug) Log.e(TAG, "baseStart illegal parameters");
            return false;
        }
        switch (opMode) {
            case Bt2Bt:
                buildDebugBt2Bt();
                break;
            case Net2Net:
                buildDebugNet2Net();
                break;
            case Record:
                buildDebugRecord();
                break;
            case AudIn2Bt:
                buildDebugAudIn2Bt();
                break;
            case Bt2AudOut:
                buildBt2AudOut();
                break;
            case AudIn2AudOut:
                buildDebugAudIn2AudOut();
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
        iCallToUiListener = null;
        iCallNetListener = null;
        iNetInfoGetter = null;
        iBluetoothListener = null;
        iDebugCtrl = null;
        opMode = null;
        callerState = null;
        iBroadcastReceiver = null;
        iService = null;
        iBtToUiCtrl = null;
        iMsgToUi = null;
        iBtList = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- data from microphone redirects to bluetooth

    private void buildDebugAudIn2Bt() {
        if (debug) Log.i(TAG, "buildDebugAudIn2Bt");
        if (iDebugCtrl == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildDebugAudIn2Bt illegal parameters");
            return;
        }

        StorageData<byte[][]> audIn2BtStorage = new StorageData<>(Tags.AUDIN2BT_STORE);

        AudIn2BtLooper audIn2BtLooper = new AudIn2BtLooper(audIn2BtStorage);

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setStorageToBt(audIn2BtStorage)
                .setmHandler(new Handler())
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi)
                .setiBtList(iBtList);
//                .addiCallNetExchangeListener(null);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugCtrl)
                .addiDebugListener(audIn2BtLooper)
//                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart();
        connectorBluetooth.baseStart();
        audIn2BtLooper.baseStart();
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Log.i(TAG, "buildBt2AudOut");
        if (iDebugCtrl == null
                || iBluetoothListener == null
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
//                .addiCallNetExchangeListener(null);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugCtrl)
                .addiDebugListener(bt2AudOutLooper)
//                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart();
        connectorBluetooth.baseStart();
        bt2AudOutLooper.baseStart();
    }

    //--------------------- data from microphone redirects to dynamic

    private void buildDebugAudIn2AudOut() {
        if (debug) Log.i(TAG, "buildDebugAudIn2AudOut");
        if (iDebugCtrl == null) {
            if (debug) Log.e(TAG, "buildDebugAudIn2AudOut illegal parameters");
            return;
        }

        AudIn2AudOutLooper audIn2AudOutLooper = new AudIn2AudOutLooper();

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugCtrl)
                .addiDebugListener(audIn2AudOutLooper)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart();
        audIn2AudOutLooper.baseStart();
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildDebugBt2Bt() {
        if (debug) Log.i(TAG, "buildDebugBt2Bt");
        if (iDebugCtrl == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildDebugBt2Bt illegal parameters");
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
//                .addiCallNetExchangeListener(null);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtLooper)
                .addiDebugListener(iDebugCtrl)
//                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);
//                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart();
        connectorBluetooth.baseStart();
        connectorBluetooth.baseCreate();
        bt2BtLooper.baseStart();
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildDebugRecord() {
        if (debug) Log.i(TAG, "buildDebugRecord");
        if (iDebugCtrl == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null
                || iBtList == null) {
            if (debug) Log.e(TAG, "buildDebugBt2Bt illegal parameters");
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
//                .addiCallNetExchangeListener(null);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtRecorder)
                .addiDebugListener(iDebugCtrl)
//                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);
//                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart();
        connectorBluetooth.baseStart();
        bt2BtRecorder.baseStart();
    }

    //--------------------- data from network looped back to network

    private void buildDebugNet2Net() {
        if (debug) Log.i(TAG, "buildDebugNet2Net");
        if (iDebugCtrl == null) {
            Log.e(TAG, "buildDebugNet2Net illegal parameters");
            return;
        }
    }

    //--------------------- data from bluetooth redirects to network and vice versa

    private void buildNormal() {
        if (debug) Log.i(TAG, "buildNormal");
        if (iCallNetListener == null
                || iNetInfoGetter == null
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
                //.addiCallNetExchangeListener(null);

        ConnectorNet connectorNet = ConnectorNet.getInstance()
                .setStorageToNet(storageBtToNet)
                .setStorageFromNet(storageNetToBt)
                .addiCallNetworkListener(iCallNetListener)
//                .addiCallNetworkExchangeListener(connectorBluetooth)
                .setiNetInfoGetter(iNetInfoGetter)
                .setHandler(handlerExtended);

        CallUi callUi = CallUi.getInstance()
                .addiCallUiListener(iCallToUiListener)
//                .addiCallUiExchangeListener(connectorBluetooth)
                .addiCallUiListener(connectorNet);

        callUi.baseStart();
        connectorBluetooth.baseStart();
        connectorNet.baseStart();
    }

}
