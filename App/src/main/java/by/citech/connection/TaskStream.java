package by.citech.connection;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.DataSource;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class TaskStream extends AsyncTask<String, IStreamCtrl, Void> {
    private IStreamCtrlRegister iStreamCtrlRegister;
    private ITransmitter iTransmitter;
    private DataSource dataSource;
    private StorageData storageBtToNet;

    public TaskStream(IStreamCtrlRegister iStreamCtrlRegister, ITransmitter iTransmitter, DataSource dataSource) {
        this.iStreamCtrlRegister = iStreamCtrlRegister;
        this.iTransmitter = iTransmitter;
        this.dataSource = dataSource;
    }

    public TaskStream(IStreamCtrlRegister iStreamCtrlRegister, ITransmitter iTransmitter, DataSource dataSource, StorageData storageBtToNet) {
        this.iStreamCtrlRegister = iStreamCtrlRegister;
        this.iTransmitter = iTransmitter;
        this.dataSource = dataSource;
        this.storageBtToNet = storageBtToNet;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (Settings.debug) Log.i(Tags.NET_TASK_STREAM, "doInBackground");
        switch (dataSource) {
            case MICROPHONE:
                final StreamAudio streamAudio = new StreamAudio(iTransmitter, Integer.parseInt(params[0]));
                publishProgress(streamAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        streamAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                final StreamBluetooth streamBluetooth = new StreamBluetooth(iTransmitter, Settings.bufferSize, storageBtToNet);
                publishProgress(streamBluetooth.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        streamBluetooth.run();
                    }
                }).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IStreamCtrl... iStreamCtrl) {
        if (Settings.debug) Log.i(Tags.NET_TASK_STREAM, "onProgressUpdate");
        iStreamCtrlRegister.registerStreamCtrl(iStreamCtrl[0]);
    }
}