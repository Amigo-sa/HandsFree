package by.citech.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class RedirectToNet
        extends AsyncTask<String, ITransmitterCtrl, Void> {

    private static final String TAG = Tags.REDIRECT2NET;
    private static final boolean debug = Settings.debug;

    private ITransmitterCtrlReg iTransmitterCtrlReg;
    private ITransmitter iTransmitter;
    private StorageData<byte[]> storageBtToNet;

    public RedirectToNet(ITransmitterCtrlReg iTransmitterCtrlReg, ITransmitter iTransmitter, StorageData<byte[]> storageBtToNet) {
        this.iTransmitterCtrlReg = iTransmitterCtrlReg;
        this.iTransmitter = iTransmitter;
        this.storageBtToNet = storageBtToNet;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        ITransmitterCtrl iTransmitterCtrl;
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                iTransmitterCtrl = new FromMic(iTransmitter);
                iTransmitterCtrl.prepareStream();
                publishProgress(iTransmitterCtrl);
                new Thread(iTransmitterCtrl::streamOn).start();
                break;
            case BLUETOOTH:
                if (debug) Log.i(TAG, "doInBackground bluetooth");
                if (storageBtToNet == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                iTransmitterCtrl = new ToNet(iTransmitter, storageBtToNet);
                iTransmitterCtrl.prepareStream();
                publishProgress(iTransmitterCtrl);
                new Thread(iTransmitterCtrl::streamOn).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(ITransmitterCtrl... iTransmitterCtrl) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iTransmitterCtrlReg.registerTransmitterCtrl(iTransmitterCtrl[0]);
    }
}