package by.citech.network.control.redirect;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.network.control.IReceiverListener;
import by.citech.network.control.IReceiverListenerReg;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiverListener {
    private int bufferSize;
    private IReceiverListenerReg iReceiverListenerReg;
    private boolean isRedirecting = false;
    private final StorageData storageNetToBt;

    RedirectToBluetooth(IReceiverListenerReg iReceiverListenerReg, int bufferSize, StorageData storageNetToBt) {
        this.iReceiverListenerReg = iReceiverListenerReg;
        this.bufferSize = bufferSize;
        this.storageNetToBt = storageNetToBt;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "start");
        redirectOff();
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "startClient");
        isRedirecting = true;
        iReceiverListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "startClient done");
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "redirectOff");
        isRedirecting = false;
        iReceiverListenerReg.registerReceiverListener(null);
        //TODO: разобраться с синхронайзом
//        synchronized (storageNetToBt) {
//            storageNetToBt.notify();
//        }
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "onReceiveMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }
}
