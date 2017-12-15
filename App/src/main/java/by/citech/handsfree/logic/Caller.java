package by.citech.handsfree.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IBaseAdder;
import by.citech.handsfree.common.IService;
import by.citech.handsfree.common.IBroadcastReceiver;
import by.citech.handsfree.gui.IBtToUiCtrl;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.Bt2AudOutLooper;
import by.citech.handsfree.debug.Bt2BtLooper;
import by.citech.handsfree.debug.Bt2BtRecorder;
import by.citech.handsfree.debug.AudIn2AudOutLooper;
import by.citech.handsfree.debug.AudIn2BtLooper;
import by.citech.handsfree.debug.IDebugListener;
import by.citech.handsfree.exchange.IMsgToUi;
import by.citech.handsfree.gui.ICallToUiListener;
import by.citech.handsfree.gui.IUiToCallListener;
import by.citech.handsfree.network.INetInfoGetter;
import by.citech.handsfree.network.INetListener;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.OpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class Caller
        implements IBase, ISettingsCtrl, IBaseAdder {

    private final String TAG = Tags.CALLER;
    private final boolean debug = Settings.debug;

    //--------------------- settings

    private boolean isInitiated;
    private OpMode opMode;

    {
        initSettings();
    }

    @Override
    public void initSettings() {
        callerState = CallerState.Null;
        iBaseList = new ArrayList<>();
        takeSettings();
        isInitiated = true;
    }

    @Override
    public void takeSettings() {
        opMode = Settings.opMode;
    }

    //--------------------- non-settings

    private volatile CallerState callerState;
    private ICallToUiListener iCallToUiListener;
    private ICallNetListener iCallNetListener;
    private INetInfoGetter iNetInfoGetter;
    private IBluetoothListener iBluetoothListener;
    private IDebugListener iDebugListener;
    private List<IBase> iBaseList;
    private IBroadcastReceiver iBroadcastReceiver;
    private IService iService;
    private IBtToUiCtrl iBtToUiCtrl;
    private IMsgToUi iMsgToUi;

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
        } else if (!instance.isInitiated) {
            instance.initSettings();
        }
        return instance;
    }

    //--------------------- getters and setters

    public IUiToCallListener getiUiBtnGreenRedListener() {
        return CallUi.getInstance();
    }

    public INetListener getiNetworkListener() {
        return ConnectorNet.getInstance();
    }

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

    public Caller setiDebugListener(IDebugListener listener) {
        this.iDebugListener = listener;
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

    //--------------------- work with fsm

    public synchronized CallerState getCallerState() {
        if (debug) Log.i(TAG, "getCallerState is " + callerState.getName());
        return callerState;
    }

    public synchronized boolean setState(CallerState fromCallerState, CallerState toCallerState) {
        if (debug) Log.w(TAG, String.format("setState from %s to %s", fromCallerState.getName(), toCallerState.getName()));
        if (callerState == fromCallerState) {
            if (fromCallerState.availableStates().contains(toCallerState)) {
                callerState = toCallerState;
                if (callerState == CallerState.Error) {
                    callerState = CallerState.Idle;  // TODO: обработку ошибок? ожидание отклика?
                } else if (callerState == CallerState.Idle) {
                    callerState = CallerState.Idle;  // TODO: переводить не в Idle, а Null и ожидать готовность?
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
    public void baseStart(IBaseAdder iBaseAdder) {
        if (debug) Log.i(TAG, "baseStart");
        if (!isInitiated) {
            initSettings();
        }
        if (iCallToUiListener == null || iBaseAdder == null) {
            if (debug) Log.e(TAG, "baseStart illegal parameters");
            return;
        } else {
            iBaseAdder.addBase(this);
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
    }

    @Override
    public void baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        if (iBaseList != null) {
            for (IBase iBase : iBaseList) {
                if (iBase != null) {
                    iBase.baseStop();
                }
            }
            iBaseList.clear();
            iBaseList = null;
        }
        iCallToUiListener = null;
        iCallNetListener = null;
        iNetInfoGetter = null;
        iBluetoothListener = null;
        iDebugListener = null;
        opMode = null;
        callerState = null;
        iBroadcastReceiver = null;
        iService = null;
        iBtToUiCtrl = null;
        iMsgToUi = null;
        isInitiated = false;
    }

    //--------------------- data from microphone redirects to bluetooth

    private void buildDebugAudIn2Bt() {
        if (debug) Log.i(TAG, "buildDebugAudIn2Bt");
        if (iDebugListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null) {
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
                .setiMsgToUi(iMsgToUi);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(audIn2BtLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart(this);
        connectorBluetooth.baseStart(this);
        audIn2BtLooper.baseStart(this);
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Log.i(TAG, "buildBt2AudOut");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null) {
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
                .setiMsgToUi(iMsgToUi);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(bt2AudOutLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart(this);
        connectorBluetooth.baseStart(this);
        bt2AudOutLooper.baseStart(this);
    }

    //--------------------- data from microphone redirects to dynamic

    private void buildDebugAudIn2AudOut() {
        if (debug) Log.i(TAG, "buildDebugAudIn2AudOut");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugAudIn2AudOut illegal parameters");
            return;
        }

        AudIn2AudOutLooper audIn2AudOutLooper = new AudIn2AudOutLooper();

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(audIn2AudOutLooper)
                .addiCallUiListener(iCallToUiListener);

        callUi.baseStart(this);
        audIn2AudOutLooper.baseStart(this);
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildDebugBt2Bt() {
        if (debug) Log.i(TAG, "buildDebugBt2Bt");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null) {
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
                .setiMsgToUi(iMsgToUi);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtLooper)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart(this);
        connectorBluetooth.baseStart(this);
        bt2BtLooper.baseStart(this);
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildDebugRecord() {
        if (debug) Log.i(TAG, "buildDebugRecord");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iBroadcastReceiver == null
                || iBtToUiCtrl == null
                || iMsgToUi == null) {
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
                .setiMsgToUi(iMsgToUi);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtRecorder)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallToUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart(this);
        connectorBluetooth.baseStart(this);
        bt2BtRecorder.baseStart(this);
    }

    //--------------------- data from network looped back to network

    private void buildDebugNet2Net() {
        if (debug) Log.i(TAG, "buildDebugNet2Net");
        if (iDebugListener == null) {
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
                || iMsgToUi == null) {
            Log.e(TAG, "buildNormal illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.FROM_BT_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.TO_BT_STORE);
        HandlerExtended handlerExtended = new HandlerExtended(getiNetworkListener());

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(handlerExtended)
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt)
                .setiService(iService)
                .setiBroadcastReceiver(iBroadcastReceiver)
                .setiBtToUiCtrl(iBtToUiCtrl)
                .setiMsgToUi(iMsgToUi);

        ConnectorNet connectorNet = ConnectorNet.getInstance()
                .setStorageToNet(storageBtToNet)
                .setStorageFromNet(storageNetToBt)
                .addiCallNetworkListener(iCallNetListener)
                .addiCallNetworkExchangeListener(connectorBluetooth)
                .setiNetInfoGetter(iNetInfoGetter)
                .setHandler(handlerExtended);

        CallUi callUi = CallUi.getInstance()
                .addiCallUiListener(iCallToUiListener)
                .addiCallUiExchangeListener(connectorBluetooth)
                .addiCallUiListener(connectorNet);

        callUi.baseStart(this);
        connectorBluetooth.baseStart(this);
        connectorNet.baseStart(this);
    }

    @Override
    public void addBase(IBase iBase) {
        if (debug) Log.i(TAG, "addBase");
        if (iBaseList == null || iBase == null) {
            Log.e(TAG, "addBase iBaseList or iBase is null");
        } else {
            iBaseList.add(iBase);
        }
    }

}
