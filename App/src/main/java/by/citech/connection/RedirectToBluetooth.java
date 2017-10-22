package by.citech.connection;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiverListener {
    private int bufferSize;
    private IReceiverListenerRegister iReceiverListenerRegister;
    private boolean isRedirecting = false;
    private final StorageData storageNetToBt;

    RedirectToBluetooth(IReceiverListenerRegister iReceiverListenerRegister, int bufferSize, StorageData storageNetToBt) {
        this.iReceiverListenerRegister = iReceiverListenerRegister;
        this.bufferSize = bufferSize;
        this.storageNetToBt = storageNetToBt;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "start");
        redirectOff();
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "run");
        isRedirecting = true;
        iReceiverListenerRegister.registerReceiverListener(this);
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "run done");
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "redirectOff");
        isRedirecting = false;
        iReceiverListenerRegister.registerReceiverListener(null);
        synchronized (storageNetToBt) {
            storageNetToBt.notify();
        }
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "onReceiveMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }
}
