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
        Log.i(Tags.CLT_TASK_CONN, "doInBackground");
        Log.i(Tags.CLT_TASK_CONN, url[0]);
        iClientCtrl = new ClientCtrlOkWebSocket(url[0], handler).startClient();
        publishProgress(iClientCtrl);
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.CLT_TASK_CONN, "doInBackground iClientCtrl is null");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IClientCtrl... iClientCtrl) {
        Log.i(Tags.CLT_TASK_CONN, "onProgressUpdate");
        iClientCtrlReg.registerClientCtrl(iClientCtrl[0]);
    }

}