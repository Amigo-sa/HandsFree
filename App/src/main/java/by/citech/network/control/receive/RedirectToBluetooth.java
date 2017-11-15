package by.citech.network.control.receive;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.network.control.IReceiveListener;
import by.citech.network.control.IReceiveListenerReg;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiveListener {
    private static final String TAG = Tags.NET_REDIR_BLUETOOTH;
    private static final boolean debug = Settings.debug;
    private IReceiveListenerReg iReceiveListenerReg;
    private boolean isRedirecting = false;
    private final StorageData storageNetToBt;

    RedirectToBluetooth(IReceiveListenerReg iReceiveListenerReg, StorageData storageNetToBt) {
        this.iReceiveListenerReg = iReceiveListenerReg;
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
        iReceiveListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(TAG, "run done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        storageNetToBt.clear();
        iReceiveListenerReg.registerReceiverListener(null);
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (debug) Log.i(TAG, "onReceiveMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }

}
