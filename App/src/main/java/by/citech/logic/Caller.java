package by.citech.logic;

import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;
import by.citech.data.StorageData;
import by.citech.debug.Bt2AudOutLooper;
import by.citech.debug.Bt2BtLooper;
import by.citech.debug.Bt2BtRecorder;
import by.citech.debug.AudIn2AudOutLooper;
import by.citech.debug.AudIn2BtLooper;
import by.citech.debug.IDebugCtrl;
import by.citech.debug.IDebugListener;
import by.citech.gui.ICallUiListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetInfoListener;
import by.citech.network.INetListener;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller {

    private final String TAG = Tags.CALLER;
    private final boolean debug = Settings.debug;

    //--------------------- settings

    private boolean isInitiated;
    private OpMode opMode;

    {
        initiate();
    }

    private void initiate() {
        isInitiated = true;
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        opMode = Settings.opMode;
    }

    private void applySettings() {
    }

    //--------------------- non-settings

    private volatile CallerState callerState;
    private ICallUiListener iCallUiListener;
    private ICallNetListener iCallNetworkListener;
    private INetInfoListener iNetInfoListener;
    private IBluetoothListener iBluetoothListener;
    private IDebugListener iDebugListener;
    private IDebugCtrl iDebugCtrl;
    private ConnectorBluetooth connectorBluetooth;
    private ConnectorNet connectorNet;
    private CallUi callUi;

    //--------------------- singleton

    private static volatile Caller instance = null;

    private Caller() {
        callerState = CallerState.Null;
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

    public IUiBtnGreenRedListener getiUiBtnGreenRedListener() {
        return CallUi.getInstance();
    }

    public INetListener getiNetworkListener() {
        return ConnectorNet.getInstance();
    }

    public ConnectorBluetooth getConnectorBluetooth() {
        return ConnectorBluetooth.getInstance();
    }

    public ServiceConnection getServiceConnection() {
        return ConnectorBluetooth.getInstance().mServiceConnection;
    }

    public Caller setiCallUiListener(ICallUiListener listener) {
        iCallUiListener = listener;
        return this;
    }

    public Caller setiCallNetListener(ICallNetListener listener) {
        iCallNetworkListener = listener;
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

    public Caller setiDebugListener(IDebugListener iDebugListener) {
        this.iDebugListener = iDebugListener;
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

    public void build() {
        if (debug) Log.i(TAG, "build");
        if (!isInitiated) {
            initiate();
        }
        if (iCallUiListener == null
                || iCallNetworkListener == null
                || iNetInfoListener == null
                || iBluetoothListener == null) {
            if (debug) Log.e(TAG, "build illegal parameters");
            return;
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

    public void stop() {
        if (debug) Log.i(TAG, "stop");
        isInitiated = false;
        callerState = CallerState.Null;
        if (iDebugCtrl != null) {
            iDebugCtrl.deactivate();
            iDebugCtrl = null;
        }
        if (connectorBluetooth != null) {
            connectorBluetooth = null;
        }
        if (callUi != null) {
            callUi.destruct();
            callUi = null;
        }
        if (connectorNet != null) {
            connectorNet.stop();
            connectorNet = null;
        }
    }

    //--------------------- data from microphone redirects to bluetooth

    private void buildDebugAudIn2Bt() {
        if (debug) Log.i(TAG, "buildDebugAudIn2Bt");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugAudIn2Bt illegal parameters");
            return;
        }

        StorageData<byte[][]> audIn2BtStorage = new StorageData<>(Tags.AUDIN2BT_STORE);

        AudIn2BtLooper audIn2BtLooper = new AudIn2BtLooper(audIn2BtStorage);
        iDebugCtrl = audIn2BtLooper;

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setStorageToBt(audIn2BtStorage)
                .setmHandler(new Handler());


        callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(audIn2BtLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener);

        connectorBluetooth.build();
        iDebugCtrl.activate();
    }

    //--------------------- data from bluetooth redirects to dynamic

    private void buildBt2AudOut() {
        if (debug) Log.i(TAG, "buildBt2AudOut");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildBt2AudOut illegal parameters");
            return;
        }

        Bt2AudOutLooper bt2AudOutLooper = new Bt2AudOutLooper();
        iDebugCtrl = bt2AudOutLooper;

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .addIRxDataListener(bt2AudOutLooper)
                .setmHandler(new Handler());

        callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(bt2AudOutLooper)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener);

        connectorBluetooth.build();
        iDebugCtrl.activate();
    }

    //--------------------- data from microphone redirects to dynamic

    private void buildDebugAudIn2AudOut() {
        if (debug) Log.i(TAG, "buildDebugAudIn2AudOut");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugAudIn2AudOut illegal parameters");
            return;
        }

        AudIn2AudOutLooper audIn2AudOutLooper = new AudIn2AudOutLooper();
        iDebugCtrl = audIn2AudOutLooper;

        callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
                .addiDebugListener(audIn2AudOutLooper)
                .addiCallUiListener(iCallUiListener);

        iDebugCtrl.activate();
    }

    //--------------------- data from bluetooth loops back to bluetooth

    private void buildDebugBt2Bt() {
        if (debug) Log.i(TAG, "buildDebugBt2Bt");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBt2Net = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNet2Bt = new StorageData<>(Tags.NET2BLE_STORE);

        Bt2BtLooper bt2BtLooper = new Bt2BtLooper(storageBt2Net, storageNet2Bt);
        iDebugCtrl = bt2BtLooper;

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageBt2Net)
                .setStorageToBt(storageNet2Bt);

        callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtLooper)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        connectorBluetooth.build();
        iDebugCtrl.activate();
    }

    //--------------------- data from bluetooth recorded and looped back to bluetooth

    private void buildDebugRecord() {
        if (debug) Log.i(TAG, "buildDebugRecord");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugBt2Bt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.NET2BLE_STORE);

        Bt2BtRecorder bt2BtRecorder = new Bt2BtRecorder(storageBtToNet, storageNetToBt);
        iDebugCtrl = bt2BtRecorder;

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt);


        callUi = CallUi.getInstance()
                .addiDebugListener(bt2BtRecorder)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        connectorBluetooth.build();
        iDebugCtrl.activate();
    }

    //--------------------- data from network looped back to network

    private void buildDebugNet2Net() {
        if (debug) Log.i(TAG, "buildDebugNet2Net");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugNet2Net illegal parameters");
            return;
        }
    }

    //--------------------- data from bluetooth redirects to network and vice versa

    private void buildNormal() {
        if (debug) Log.i(TAG, "buildNormal");
        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.NET2BLE_STORE);
        HandlerExtended handlerExtended = new HandlerExtended(getiNetworkListener());

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(handlerExtended)
                .setStorageFromBt(storageBtToNet)
                .setStorageToBt(storageNetToBt);

        connectorNet = ConnectorNet.getInstance()
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt)
                .addiCallNetworkListener(iCallNetworkListener)
                .addiCallNetworkExchangeListener(connectorBluetooth)
                .setiNetInfoListener(iNetInfoListener)
                .setHandler(handlerExtended);

        callUi = CallUi.getInstance()
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth)
                .addiCallUiListener(connectorNet);

        connectorBluetooth.build();
        connectorNet.build();
    }

}
