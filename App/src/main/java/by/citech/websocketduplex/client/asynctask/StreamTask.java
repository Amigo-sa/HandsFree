package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;

import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.websocket.OkWebSocketClientCtrl;

public class StreamTask extends AsyncTask<String, String, Void> {
    private ClientActivity activity;
    private OkWebSocketClientCtrl clientCtrl;

    public StreamTask(ClientActivity activity, OkWebSocketClientCtrl clientCtrl) {
        this.activity = activity;
        this.clientCtrl = clientCtrl;

    }

    @Override
    protected Void doInBackground(String... params) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
}
