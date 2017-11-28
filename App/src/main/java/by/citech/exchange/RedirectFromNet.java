package by.citech.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class RedirectFromNet
        extends AsyncTask<String, IReceiverCtrl, Void> {

    private static final String TAG = Tags.NET_REDIR;
    private static final boolean debug = Settings.debug;

    private IReceiverCtrlReg iReceiverCtrlReg;
    private IReceiverReg iReceiverReg;
    private StorageData<byte[][]> storageNetToBt;

    public RedirectFromNet(IReceiverCtrlReg iReceiverCtrlReg, IReceiverReg iReceiverReg, StorageData<byte[][]> storageNetToBt) {
        if (debug) Log.i(TAG, "RedirectFromNet");
        this.iReceiverCtrlReg = iReceiverCtrlReg;
        this.iReceiverReg = iReceiverReg;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                final ToAudio toAudio = new ToAudio(iReceiverReg);
                toAudio.prepare();
                publishProgress(toAudio);
                new Thread(toAudio::run).start();
                break;
            case BLUETOOTH:
                Log.i(TAG, "doInBackground bluetooth");
                if (storageNetToBt == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                final ToBluetooth toBluetooth = new ToBluetooth(iReceiverReg, storageNetToBt);
                toBluetooth.prepare();
                publishProgress(toBluetooth);
                new Thread(toBluetooth::run).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IReceiverCtrl... iReceiverCtrl) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iReceiverCtrlReg.registerReceiverCtrl(iReceiverCtrl[0]);
    }

}
