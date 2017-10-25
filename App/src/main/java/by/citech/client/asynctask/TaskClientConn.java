package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientCtrlReg;
import by.citech.client.network.ClientCtrlOkWebSocket;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class TaskClientConn extends AsyncTask<String, IClientCtrl, Void> {
    private IClientCtrlReg iClientCtrlReg;
    private Handler handler;
    private IClientCtrl iClientCtrl;

    public TaskClientConn(IClientCtrlReg iClientCtrlReg, Handler handler) {
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