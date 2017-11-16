package by.citech.network.control.transmit;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Stream extends AsyncTask<String, IStreamCtrl, Void> {
    private static final String TAG = Tags.NET_STREAM;
    private static final boolean debug = Settings.debug;
    private IStreamCtrlReg iStreamCtrlReg;
    private ITransmitter iTransmitter;
    private StorageData<byte[]> storageBtToNet;

    public Stream(IStreamCtrlReg iStreamCtrlReg, ITransmitter iTransmitter, StorageData<byte[]> storageBtToNet) {
        this.iStreamCtrlReg = iStreamCtrlReg;
        this.iTransmitter = iTransmitter;
        this.storageBtToNet = storageBtToNet;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                final StreamAudio streamAudio = new StreamAudio(iTransmitter);
                publishProgress(streamAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        streamAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                if (debug) Log.i(TAG, "doInBackground bluetooth");
                if (storageBtToNet == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                final StreamBluetooth streamBluetooth = new StreamBluetooth(iTransmitter, storageBtToNet);
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
        if (debug) Log.i(TAG, "onProgressUpdate");
        iStreamCtrlReg.registerStreamCtrl(iStreamCtrl[0]);
    }
}