package by.citech.handsfree.network.client;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ClientConn
        extends AsyncTask<String, IClientCtrl, Void> {

    private IClientCtrlReg iClientCtrlReg;
    private Handler handler;
    private IClientCtrl iClientCtrl;

    public ClientConn(IClientCtrlReg iClientCtrlReg, Handler handler) {
        this.iClientCtrlReg = iClientCtrlReg;
        this.handler = handler;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.ClientConn, "doInBackground");
        Log.i(Tags.ClientConn, url[0]);
        iClientCtrl = new Client(url[0], handler).startClient();
        publishProgress(iClientCtrl);
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.ClientConn, "doInBackground iClientCtrl is null");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IClientCtrl... iClientCtrl) {
        Log.i(Tags.ClientConn, "onProgressUpdate");
        iClientCtrlReg.registerClientCtrl(iClientCtrl[0]);
    }

}