package by.citech.logic;

import android.os.Handler;
import android.util.Log;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Caller {

    private volatile State state = State.Null;
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;
    private ICallUiListener iCallUiListener;
    private ICallNetworkListener iCallNetworkListener;
    private INetworkInfoListener iNetworkInfoListener;
    private Handler handler;

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

    public StorageData getStorageBtToNet() {
        return storageBtToNet;
    }

    public StorageData getStorageNetToBt() {
        return storageNetToBt;
    }

    public Caller setStorageBtToNet(StorageData storageBtToNet) {
        this.storageBtToNet = storageBtToNet;
        return this;
    }

    public Caller setStorageNetToBt(StorageData storageNetToBt) {
        this.storageNetToBt = storageNetToBt;
        return this;
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

    public Caller setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public synchronized State getState() {
        if (Settings.debug) Log.i(Tags.CALLER, "getState is " + state.getName());
        return state;
    }

    public synchronized boolean setState(State fromState, State toState) {
        if (Settings.debug) Log.w(Tags.CALLER, String.format("setState from %s to %s", fromState.getName(), toState.getName()));
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
                if (Settings.debug) Log.e(Tags.CALLER, String.format("setState: %s is not available from %s", toState.getName(), fromState.getName()));
            }
        } else {
            if (Settings.debug) Log.e(Tags.CALLER, String.format("setState: current is not %s", fromState.getName()));
        }
        return false;
    }

    //--------------------- main

    public void start() {
        if (storageBtToNet == null
                || storageNetToBt == null
                || iCallUiListener == null
                || iCallNetworkListener == null
                || iNetworkInfoListener == null
                || handler == null) {
            if (Settings.debug) Log.e(Tags.CALLER, "start at least one of key parameters are null");
            return;
        }

        CallUi.getInstance()
                .addiCallUiListener(iCallUiListener)
                .addiCallUiListener(ConnectorNetwork.getInstance());

        ConnectorNetwork.getInstance()
                .addiCallNetworkListener(iCallNetworkListener)
                .setiNetworkInfoListener(iNetworkInfoListener)
                .setHandler(handler)
                .start();
    }

    public void stop() {
        // TODO: добавить очищение хранилищ?
        ConnectorNetwork.getInstance().stop();
    }
}
