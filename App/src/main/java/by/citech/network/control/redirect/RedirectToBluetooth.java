package by.citech.network.control.redirect;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.network.control.IReceiverListener;
import by.citech.network.control.IReceiverListenerReg;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiverListener {
    private static final String TAG = Tags.NET_REDIR_BLUETOOTH;
    private static final boolean debug = Settings.debug;
    private IReceiverListenerReg iReceiverListenerReg;
    private boolean isRedirecting = false;
    private final StorageData storageNetToBt;

    RedirectToBluetooth(IReceiverListenerReg iReceiverListenerReg, StorageData storageNetToBt) {
        this.iReceiverListenerReg = iReceiverListenerReg;
        this.storageNetToBt = storageNetToBt;
    }

    public IRedirectCtrl start() {
        if (debug) Log.i(TAG, "start");
        redirectOff();
        return this;
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        isRedirecting = true;
        iReceiverListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(TAG, "run done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        storageNetToBt.clear();
        iReceiverListenerReg.registerReceiverListener(null);
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (debug) Log.i(TAG, "onReceiveMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }

}
