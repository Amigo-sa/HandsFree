package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.client.network.IStream;
import by.citech.client.network.IStreamOn;
import by.citech.client.network.IClientCtrl;
import by.citech.data.StorageData;
import by.citech.param.DataSource;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class StreamTask extends AsyncTask<String, IStream, Void> {
    private IStreamOn iStreamOn;
    private IClientCtrl iClientCtrl;
    private DataSource dataSource;
    private StorageData storageBtToNet;

    public StreamTask(IStreamOn iStreamOn, IClientCtrl clientCtrl, DataSource dataSource) {
        this.iStreamOn = iStreamOn;
        this.iClientCtrl = clientCtrl;
        this.dataSource = dataSource;
    }

    public StreamTask(IStreamOn iStreamOn, IClientCtrl clientCtrl, DataSource dataSource, StorageData storageBtToNet) {
        this.iStreamOn = iStreamOn;
        this.iClientCtrl = clientCtrl;
        this.dataSource = dataSource;
        this.storageBtToNet = storageBtToNet;
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.i(Tags.CLT_TASK_STREAM, "doInBackground");

        switch (dataSource) {
            case MICROPHONE:
                StreamAudio streamAudio = new StreamAudio(iClientCtrl, Integer.parseInt(params[0]));
                publishProgress(streamAudio.start());
                streamAudio.run();
                break;
            case BLUETOOTH:
                StreamBluetooth streamBluetooth = new StreamBluetooth(iClientCtrl, Settings.bufferSize, storageBtToNet);
                publishProgress(streamBluetooth.start());
                streamBluetooth.run();
                break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(IStream... iStream) {
        Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate");
        if (iStream[0] != null) {
            iStreamOn.setStream(iStream[0]);
            Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate iStream is not null");
        } else {
            Log.i(Tags.CLT_TASK_STREAM, "onProgressUpdate iStream is null");
        }
    }
}