package by.citech.logic;

import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;
import by.citech.data.StorageData;
import by.citech.debug.DebugBtToAudLooper;
import by.citech.debug.DebugBtToBtLooper;
import by.citech.debug.DebugBtToBtRecorder;
import by.citech.debug.DebugMicToAudLooperAlter;
import by.citech.debug.DebugMicToAudLooper;
import by.citech.debug.IDebugListener;
import by.citech.gui.ICallUiListener;
import by.citech.gui.IUiBtnGreenRedListener;
import by.citech.network.INetInfoListener;
import by.citech.network.INetListener;
import by.citech.param.DebugMode;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller {

    private static final String TAG = Tags.CALLER;
    private static final boolean debug = Settings.debug;
    private static final DebugMode debugMode = Settings.debugMode;

    private volatile CallerState callerState;
    private ICallUiListener iCallUiListener;
    private ICallNetListener iCallNetworkListener;
    private INetInfoListener iNetInfoListener;
    private IBluetoothListener iBluetoothListener;
    private IDebugListener iDebugListener;
    private HandlerExtended handlerExtended;
    private DebugBtToBtLooper debugBtToBtLooper;
    private DebugBtToBtRecorder debugBtToBtRecorder;
    private DebugMicToAudLooperAlter debugMicToAudLooperAlter;
    private DebugBtToAudLooper debugBtToAudLooper;
    private DebugMicToAudLooper debugMicToAudLooper;
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

    public Caller setiCallNetworkListener(ICallNetListener listener) {
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
        if (iCallUiListener == null
                || iCallNetworkListener == null
                || iNetInfoListener == null
                || iBluetoothListener == null) {
            if (debug) Log.e(TAG, "build illegal parameters");
            return;
        }

        switch (debugMode) {
            case LoopbackBtToBt:
                buildDebugLoopbackBtToBt();
                break;
            case LoopbackNetToNet:
                buildDebugLoopbackNetToNet();
                break;
            case Record:
                buildDebugRecord();
                break;
            case BtToAudio:
                buildDebugBtToAud();
                break;
            case MicToAudio:
                buildDebugMicToAud();
                break;
            case Normal:
            default:
                buildNormal();
                break;
        }
    }

    private void buildDebugBtToAud() {
        if (debug) Log.i(TAG, "buildDebugBtToAud");
        StorageData<byte[]> storageBtToAud = new StorageData<>(Tags.BT2AUD_STORE);
    }

    private void buildDebugMicToAud() {
        if (debug) Log.i(TAG, "buildDebugMicToAud");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugMicToAud illegal parameters");
            return;
        }

//      StorageData<short[]> storageMic = new StorageData<>(Tags.MIC_STORE);
//      StorageData<short[]> sourceAud = new StorageData<>(Tags.AUD_SOURCE);
//
//      debugMicToAudLooperAlter = new DebugMicToAudLooperAlter(storageMic, sourceAud);

        debugMicToAudLooper = new DebugMicToAudLooper();

        callUi = CallUi.getInstance()
                .addiDebugListener(iDebugListener)
//              .addiDebugListener(debugMicToAudLooperAlter)
                .addiDebugListener(debugMicToAudLooper)
                .addiCallUiListener(iCallUiListener);

//      debugMicToAudLooperAlter.start();
        debugMicToAudLooper.start();
    }

    private void buildDebugLoopbackBtToBt() {
        if (debug) Log.i(TAG, "buildDebugLoopbackBtToBt");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugLoopbackBtToBt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.NET2BLE_STORE);

        debugBtToBtLooper = new DebugBtToBtLooper(storageBtToNet, storageNetToBt);

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt);

        connectorBluetooth.build();

        callUi = CallUi.getInstance()
                .addiDebugListener(debugBtToBtLooper)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        debugBtToBtLooper.start();
    }

    private void buildDebugRecord() {
        if (debug) Log.i(TAG, "buildDebugRecord");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugLoopbackBtToBt illegal parameters");
            return;
        }

        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.NET2BLE_STORE);

        debugBtToBtRecorder = new DebugBtToBtRecorder(storageBtToNet, storageNetToBt);

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt);

        connectorBluetooth.build();

        callUi = CallUi.getInstance()
                .addiDebugListener(debugBtToBtRecorder)
                .addiDebugListener(iDebugListener)
                .addiDebugListener(connectorBluetooth)
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(connectorBluetooth);

        debugBtToBtRecorder.start();
    }

    private void buildDebugLoopbackNetToNet() {
        if (debug) Log.i(TAG, "buildDebugLoopbackNetToNet");
        if (iDebugListener == null) {
            if (debug) Log.e(TAG, "buildDebugLoopbackNetToNet illegal parameters");
            return;
        }
    }

    private void buildNormal() {
        if (debug) Log.i(TAG, "buildNormal");
        StorageData<byte[]> storageBtToNet = new StorageData<>(Tags.BLE2NET_STORE);
        StorageData<byte[][]> storageNetToBt = new StorageData<>(Tags.NET2BLE_STORE);
        handlerExtended = new HandlerExtended(getiNetworkListener());

        connectorBluetooth = ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(handlerExtended)
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt);

        connectorBluetooth.build();

        callUi = CallUi.getInstance()
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(ConnectorBluetooth.getInstance())
                .addiCallUiListener(ConnectorNet.getInstance());

        connectorNet = ConnectorNet.getInstance()
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt)
                .addiCallNetworkListener(iCallNetworkListener)
                .addiCallNetworkExchangeListener(ConnectorBluetooth.getInstance())
                .setiNetInfoListener(iNetInfoListener)
                .setHandler(handlerExtended);

        startConnectorNetwork();
    }

    private void startConnectorNetwork() {
        if (debug) Log.i(TAG, "startConnectorNetwork");
        ConnectorNet.getInstance().start();
    }

    public void stop() {
        if (debug) Log.i(TAG, "stop");
        callerState = CallerState.Null;
        if (debugMicToAudLooperAlter != null) {
            debugMicToAudLooperAlter.deactivate();
            debugMicToAudLooperAlter = null;
        }
        if (debugBtToBtLooper != null) {
            debugBtToBtLooper.deactivate();
            debugBtToBtLooper = null;
        }
        if (debugBtToBtRecorder != null) {
            debugBtToBtRecorder.deactivate();
            debugBtToBtRecorder = null;
        }
        if (connectorBluetooth != null) {
            connectorBluetooth = null;
        }
        if (callUi != null) {
            callUi = null;
        }
        if (connectorNet != null) {
            connectorNet.stop();
            connectorNet = null;
        }
    }

}
