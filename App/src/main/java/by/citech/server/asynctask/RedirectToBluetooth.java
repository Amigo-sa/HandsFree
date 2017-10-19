package by.citech.websocketduplex.server.asynctask;

import android.util.Log;
import by.citech.websocketduplex.data.StorageData;
import by.citech.websocketduplex.param.Settings;
import by.citech.websocketduplex.param.Tags;
import by.citech.websocketduplex.server.network.IRedirectCtrl;
import by.citech.websocketduplex.server.network.IServerCtrl;
import by.citech.websocketduplex.server.network.IServerListener;

class RedirectToBluetooth implements IRedirectCtrl, IServerListener{
    private int bufferSize;
    private IServerCtrl serverCtrl;
    private StorageData storageNetToBt;
    private boolean isRedirecting = false;

    RedirectToBluetooth(IServerCtrl serverCtrl, int bufferSize, StorageData storageNetToBt) {
        this.serverCtrl = serverCtrl;
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
        serverCtrl.setListener(this);
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
