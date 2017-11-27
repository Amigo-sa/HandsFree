package by.citech.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class IntoNet
        extends AsyncTask<String, IIntoNetCtrl, Void> {

    private static final String TAG = Tags.NET_STREAM;
    private static final boolean debug = Settings.debug;
    private IIntoNetCtrlReg iIntoNetCtrlReg;
    private ITransmitter iTransmitter;
    private StorageData<byte[]> storageBtToNet;

    public IntoNet(IIntoNetCtrlReg iIntoNetCtrlReg, ITransmitter iTransmitter, StorageData<byte[]> storageBtToNet) {
        this.iIntoNetCtrlReg = iIntoNetCtrlReg;
        this.iTransmitter = iTransmitter;
        this.storageBtToNet = storageBtToNet;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                final IntoNetAudio intoNetAudio = new IntoNetAudio(iTransmitter);
                publishProgress(intoNetAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        intoNetAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                if (debug) Log.i(TAG, "doInBackground bluetooth");
                if (storageBtToNet == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                final IntoNetBluetooth intoNetBluetooth = new IntoNetBluetooth(iTransmitter, storageBtToNet);
                publishProgress(intoNetBluetooth.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        intoNetBluetooth.run();
                    }
                }).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IIntoNetCtrl... iIntoNetCtrl) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iIntoNetCtrlReg.registerStreamCtrl(iIntoNetCtrl[0]);
    }
}