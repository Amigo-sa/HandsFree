package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.network.IStream;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.client.network.StreamAudio;
import by.citech.websocketduplex.param.DataSource;
import by.citech.websocketduplex.param.Tags;

public class StreamTask extends AsyncTask<String, IStream, Void> {
    private ClientActivity activity;
    private OkWebSocketClientCtrl clientCtrl;
    private DataSource dataSource;

    public StreamTask(ClientActivity activity, OkWebSocketClientCtrl clientCtrl, DataSource dataSource) {
        this.activity = activity;
        this.clientCtrl = clientCtrl;
        this.dataSource = dataSource;
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.i(Tags.CLT_TASK_STREAM, "doInBackground");

        switch (dataSource) {
            case MICROPHONE:
                StreamAudio streamAudio = new StreamAudio(clientCtrl, Integer.parseInt(params[0]));
                publishProgress(streamAudio.start());
                streamAudio.run();
                break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(IStream... iStream) {
        Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate");
        if (iStream[0] != null) {
            activity.iStream = iStream[0];
            Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate iStream is not null");
        } else {
            Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate iStream is null");
        }
    }
}