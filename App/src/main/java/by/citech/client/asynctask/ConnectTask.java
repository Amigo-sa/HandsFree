package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientOn;
import by.citech.client.network.OkWebSocketClientCtrl;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ConnectTask extends AsyncTask<String, IClientCtrl, Void> {
    private IClientOn iClientOn;
    private Handler handler;
    private IClientCtrl iClientCtrl;

    public ConnectTask(IClientOn iClientOn, Handler handler) {
        this.iClientOn = iClientOn;
        this.handler = handler;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.CLT_TASK_CONN, "doInBackground");
        Log.i(Tags.CLT_TASK_CONN, url[0]);
        iClientCtrl = new OkWebSocketClientCtrl(url[0], handler).run();
        publishProgress(iClientCtrl);
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.CLT_TASK_CONN, "doInBackground iClientCtrl is null");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IClientCtrl... iClientCtrl) {
        Log.i(Tags.CLT_TASK_CONN, "onProgressUpdate");
        iClientOn.clientStarted(iClientCtrl[0]);
    }
}