package by.citech.server.asynctask;

import android.util.Log;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;
import by.citech.server.network.IRedirectCtrl;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.IServerListener;

class RedirectToBluetooth implements IRedirectCtrl, IServerListener{
    private int bufferSize;
    private IServerCtrl iServerCtrl;
    private StorageData storageNetToBt;
    private boolean isRedirecting = false;

    RedirectToBluetooth(IServerCtrl iServerCtrl, int bufferSize, StorageData storageNetToBt) {
        this.iServerCtrl = iServerCtrl;
        this.bufferSize = bufferSize;
        this.storageNetToBt = storageNetToBt;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_BLUETOOTH, "start");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_BLUETOOTH, "run");
        isRedirecting = true;
        iServerCtrl.setListener(this);
        if (Settings.debug) Log.i(Tags.SRV_REDIR_BLUETOOTH, "run done");
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_BLUETOOTH, "redirectOff");
        isRedirecting = false;
        storageNetToBt.notify();
    }

    @Override
    public void onMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_BLUETOOTH, "onMessage");
        if (isRedirecting) {
            storageNetToBt.putData(data);
        }
    }
}
