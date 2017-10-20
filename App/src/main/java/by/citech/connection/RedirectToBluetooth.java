package by.citech.connection;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToBluetooth implements IRedirectCtrl, IReceiver {
    private int bufferSize;
    private IReceiverRegister iReceiverRegister;
    private StorageData storageNetToBt;
    private boolean isRedirecting = false;

    RedirectToBluetooth(IReceiverRegister iReceiverRegister, int bufferSize, StorageData storageNetToBt) {
        this.iReceiverRegister = iReceiverRegister;
        this.bufferSize = bufferSize;
        this.storageNetToBt = storageNetToBt;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "start");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "run");
        isRedirecting = true;
        iReceiverRegister.setListener(this);
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "run done");
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "redirectOff");
        isRedirecting = false;
        storageNetToBt.notify();
    }

    @Override
    public void onMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_BLUETOOTH, "onMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }
}
