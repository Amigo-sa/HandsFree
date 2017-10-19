package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.network.IClientCtrl;
import by.citech.websocketduplex.client.network.IClientOn;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.param.Tags;

public class OpenWebSocketTask extends AsyncTask<String, Void, Void> {
    private IClientOn iClientOn;
    private Handler handler;
    private IClientCtrl clientCtrl;

    public OpenWebSocketTask (IClientOn iClientOn, Handler handler) {
        this.iClientOn = iClientOn;
        this.handler = handler;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.CLT_TASK_OWS, "doInBackground");
        Log.i(Tags.CLT_TASK_OWS, url[0]);
        clientCtrl = new OkWebSocketClientCtrl(url[0], handler);
        clientCtrl.run();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.CLT_TASK_OWS, "onPostExecute");
        iClientOn.clientStarted(clientCtrl);
    }
}