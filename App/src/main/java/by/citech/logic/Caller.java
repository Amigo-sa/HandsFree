package by.citech.logic;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import by.citech.IService;
import by.citech.bluetoothlegatt.IReceive;
import by.citech.bluetoothlegatt.IVisible;
import by.citech.data.StorageData;
import by.citech.debug.Bt2AudOutLooper;
import by.citech.debug.Bt2BtLooper;
import by.citech.debug.Bt2BtRecorder;
import by.citech.debug.AudIn2AudOutLooper;
import by.citech.debug.AudIn2BtLooper;
import by.citech.debug.IDebugListener;
import by.citech.gui.ICallUiListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetInfoListener;
import by.citech.network.INetListener;
import by.citech.param.ISettings;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller
        implements IBase, ISettings, IBaseAdder {

    private final String TAG = Tags.CALLER;
    private final boolean debug = Settings.debug;

    //--------------------- settings

    private boolean isInitiated;
    private OpMode opMode;

    {
        initiate();
    }

    @Override
    public void initiate() {
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
    private ICallUiListener iCallUiListener;
    private ICallNetListener iCallNetListener;
    private INetInfoListener iNetInfoListener;
    private IBluetoothListener iBluetoothListener;
    private IDebugListener iDebugListener;
    private List<IBase> iBaseList;
    private IReceive iReceive;
    private IService iService;
    private IVisible iVisible;

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
            instance.initiate();
        }
        return instance;
    }

    //--------------------- getters and setters

    public IUiBtnGreenRedListener getiUiBtnGreenRedListener() {
        return CallUi.getInstance();
    }

    public INetListener getiNetworkListener() {
        return ConnectorNet.getInstance();
    }

    public ConnectorBluetooth getConnectorBluetooth() {
        return ConnectorBluetooth.getInstance();
    }

    public Caller setiCallUiListener(ICallUiListener listener) {
        iCallUiListener = listener;
        return this;
    }

    public Caller setiCallNetListener(ICallNetListener listener) {
        iCallNetListener = listener;
        return this;
    }

    public Caller setiNetInfoListener(INetInfoListener listener) {
        iNetInfoListener = listener;
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

  public Caller setiReceive(IReceive iReceive) {
        this.iReceive = iReceive;
        return this;
    }

    public Caller setiService(IService iService) {
        this.iService = iService;
        return this;
    }

    public Caller setiVisible(IVisible iVisible) {
        this.iVisible = iVisible;
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
            initiate();
        }
        if (iCallUiListener == null || iBaseAdder == null) {
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
        //TODO: implement IBase.baseStop() in ConnectorBluetooth
        if (iBaseList != null) {
            for (IBase iBase : iBaseList) {
                if (iBase != null) {
                    iBase.baseStop();
                }
            }
            iBaseList.clear();
            iBaseList = null;
        }
        iCallUiListener = null;
        iCallNetListener = null;
        iNetInfoListener = null;
        iBluetoothListener = null;
        iDebugListener = null;
        opMode = null;
        callerState = null;
        iReceive = null;
        iService = null;
        iVisible = null;
        isInitiated = false;
    }

    //--------------------- data from microphone redirects to bluetooth

    private void buildDebugAudIn2Bt() {
        if (debug) Log.i(TAG, "buildDebugAudIn2Bt");
        if (iDebugListener == null
                || iService == null
                || iReceive == null
                || iVisible == null) {
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
                .setiReceive(iReceive)
                .setiVisible(iVisible);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(audIn2BtLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener);

        callUi.baseStart(this);
        connectorBluetooth.build();
        audIn2BtLooper.baseStart(this);
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Log.i(TAG, "buildBt2AudOut");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iReceive == null
                || iVisible == null) {
            if (debug) Log.e(TAG, "buildBt2AudOut illegal parameters");
            return;
        }

        Bt2AudOutLooper bt2AudOutLooper = new Bt2AudOutLooper();

        ConnectorBluetooth connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .addIRxDataListener(bt2AudOutLooper)
                .setmHandler(new Handler())
                .setiService(iService)
                .setiReceive(iReceive)
                .setiVisible(iVisible);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(bt2AudOutLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener);

        callUi.baseStart(this);
        connectorBluetooth.build();
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
                .addiCallUiListener(iCallUiListener);

        callUi.baseStart(this);
        audIn2AudOutLooper.baseStart(this);
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildDebugBt2Bt() {
        if (debug) Log.i(TAG, "buildDebugBt2Bt");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iReceive == null
                || iVisible == null) {
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
                .setiReceive(iReceive)
                .setiVisible(iVisible);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtLooper)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart(this);
        connectorBluetooth.build();
        bt2BtLooper.baseStart(this);
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildDebugRecord() {
        if (debug) Log.i(TAG, "buildDebugRecord");
        if (iDebugListener == null
                || iBluetoothListener == null
                || iService == null
                || iReceive == null
                || iVisible == null) {
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
                .setiReceive(iReceive)
                .setiVisible(iVisible);

        CallUi callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtRecorder)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        callUi.baseStart(this);
        connectorBluetooth.build();
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
                || iNetInfoListener == null
                || iBluetoothListener == null
                || iService == null
                || iReceive == null
                || iVisible == null) {
            Log.e(TAG, "buildNormal illegal parameters");
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
                .setiReceive(iReceive)
                .setiVisible(iVisible);

        ConnectorNet connectorNet = ConnectorNet.getInstance()
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt)
                .addiCallNetworkListener(iCallNetListener)
                .addiCallNetworkExchangeListener(connectorBluetooth)
                .setiNetInfoListener(iNetInfoListener)
                .setHandler(handlerExtended);

        CallUi callUi = CallUi.getInstance()
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth)
                .addiCallUiListener(connectorNet);

        callUi.baseStart(this);
        connectorBluetooth.build();
        connectorNet.baseStart(this);
    }

    @Override
    public void addBase(IBase iBase) {
        if (iBaseList == null || iBase == null) {
            Log.e(TAG, "addBase iBaseList or iBase is null");
        } else {
            iBaseList.add(iBase);
        }
    }

}
