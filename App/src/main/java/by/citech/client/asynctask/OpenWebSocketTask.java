package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.param.Tags;

public class OpenWebSocketTask extends AsyncTask<String, Void, Void> {
    private ClientActivity activity;
    private Handler handler;
    private OkWebSocketClientCtrl clientCtrl;

    public OpenWebSocketTask (ClientActivity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.CLT_TASK_OWS, "doInBackground");
        Log.i(Tags.CLT_TASK_OWS, url[0]);
        clientCtrl = new OkWebSocketClientCtrl(url[0], handler);
        clientCtrl.run();
        publishProgress();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.CLT_TASK_OWS, "onPostExecute");
        activity.clientCtrl = clientCtrl;
    }
}