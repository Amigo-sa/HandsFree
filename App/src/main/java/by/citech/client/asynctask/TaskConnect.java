package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientCtrlRegister;
import by.citech.client.network.ClientCtrlOkWebSocket;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class TaskConnect extends AsyncTask<String, IClientCtrl, Void> {
    private IClientCtrlRegister iClientCtrlRegister;
    private Handler handler;
    private IClientCtrl iClientCtrl;

    public TaskConnect(IClientCtrlRegister iClientCtrlRegister, Handler handler) {
        this.iClientCtrlRegister = iClientCtrlRegister;
        this.handler = handler;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.CLT_TASK_CONN, "doInBackground");
        Log.i(Tags.CLT_TASK_CONN, url[0]);
        iClientCtrl = new ClientCtrlOkWebSocket(url[0], handler).run();
        publishProgress(iClientCtrl);
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.CLT_TASK_CONN, "doInBackground iClientCtrl is null");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IClientCtrl... iClientCtrl) {
        Log.i(Tags.CLT_TASK_CONN, "onProgressUpdate");
        iClientCtrlRegister.registerClientCtrl(iClientCtrl[0]);
    }
}