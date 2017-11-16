package by.citech.logic;

import android.content.ServiceConnection;
import android.os.Handler;
import android.util.Log;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller {

    private static final String TAG = Tags.CALLER;
    private static final boolean debug = Settings.debug;
    private volatile State state = State.Null;
    private ICallUiListener iCallUiListener;
    private ICallNetworkListener iCallNetworkListener;
    private INetworkInfoListener iNetworkInfoListener;
    private IBluetoothListener iBluetoothListener;

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

    public IUiBtnGreenRedListener getiUiBtnGreenRedListener() {
        return CallUi.getInstance();
    }

    public INetworkListener getiNetworkListener() {
        return ConnectorNetwork.getInstance();
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

    public Caller setiCallNetworkListener(ICallNetworkListener listener) {
        iCallNetworkListener = listener;
        return this;
    }

    public Caller setiNetworkInfoListener(INetworkInfoListener listener) {
        iNetworkInfoListener = listener;
        return this;
    }

    public Caller setiBluetoothListener(IBluetoothListener listener) {
        iBluetoothListener = listener;
        return this;
    }

    //--------------------- work with fsm

    public synchronized State getState() {
        if (debug) Log.i(TAG, "getState is " + state.getName());
        return state;
    }

    public synchronized boolean setState(State fromState, State toState) {
        if (debug) Log.w(TAG, String.format("setState from %s to %s", fromState.getName(), toState.getName()));
        if (state == fromState) {
            if (fromState.availableStates().contains(toState)) {
                state = toState;
                if (state == State.Error) {
                    state = State.Idle;  // TODO: обработку ошибок? ожидание отклика?
                } else if (state == State.Idle) {
                    state = State.Idle;  // TODO: переводить не в Idle, а Null и ожидать готовность?
                }
                return true;
            } else {
                if (debug) Log.e(TAG, String.format("setState: %s is not available from %s", toState.getName(), fromState.getName()));
            }
        } else {
            if (debug) Log.e(TAG, String.format("setState: current is not %s", fromState.getName()));
        }
        return false;
    }

    //--------------------- main

    public void build() {
        if (iCallUiListener == null
                || iCallNetworkListener == null
                || iNetworkInfoListener == null
                || iBluetoothListener == null) {
            if (debug) Log.e(TAG, "build at least one of key parameters are null");
            return;
        }

        // хранилища данных
        StorageData storageBtToNet = new StorageData<byte[]>(Tags.BLE2NET_STORE);
        StorageData storageNetToBt = new StorageData<byte[][]>(Tags.NET2BLE_STORE);

        CallUi.getInstance()
                .addiCallUiListener(iCallUiListener)
                .addiCallUiExchangeListener(ConnectorBluetooth.getInstance())
                .addiCallUiListener(ConnectorNetwork.getInstance());

        ConnectorNetwork.getInstance()
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt)
                .addiCallNetworkListener(iCallNetworkListener)
                .addiCallNetworkExchangeListener(ConnectorBluetooth.getInstance())
                .setiNetworkInfoListener(iNetworkInfoListener)
                .setHandler(new HandlerExtended(getiNetworkListener()));

        ConnectorBluetooth.getInstance()
                .setiBluetoothListener(iBluetoothListener)
                .setmHandler(new Handler())
                .setStorageBtToNet(storageBtToNet)
                .setStorageNetToBt(storageNetToBt);

        startConnectorNetwork();
    }

    private void startConnectorNetwork() {
        ConnectorNetwork.getInstance().start();
    }

    public void stop() {
        // TODO: добавить очищение хранилищ?
        ConnectorNetwork.getInstance().stop();
    }

}
