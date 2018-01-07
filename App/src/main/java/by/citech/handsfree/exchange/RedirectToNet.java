package by.citech.handsfree.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.consumers.ToNet;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.enumeration.DataSource;
import by.citech.handsfree.threading.IThreadManager;

public class RedirectToNet
        extends AsyncTask<DataSource, ITransmitterCtrl, Void>
        implements IThreadManager {

    private static final String TAG = Tags.REDIR2NET;
    private static final boolean debug = Settings.debug;

    private ITransmitterCtrlReg iTransmitterCtrlReg;
    private ITransmitter iTransmitter;
    private StorageData<byte[]> storageToNet;

    public RedirectToNet(ITransmitterCtrlReg iTransmitterCtrlReg, ITransmitter iTransmitter, StorageData<byte[]> storageToNet) {
        this.iTransmitterCtrlReg = iTransmitterCtrlReg;
        this.iTransmitter = iTransmitter;
        this.storageToNet = storageToNet;
    }

    @Override
    protected Void doInBackground(DataSource... params) {
        if (debug) Log.i(TAG, "doInBackground");
        ITransmitterCtrl iTransmitterCtrl;
        switch (params[0]) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                iTransmitterCtrl = new FromAudioIn();
                try {
                    iTransmitterCtrl.prepareStream(iTransmitter);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iTransmitterCtrl);
                addRunnable(iTransmitterCtrl::streamOn);
                break;
            case BLUETOOTH:
                if (debug) Log.i(TAG, "doInBackground bluetooth");
                try {
                    iTransmitterCtrl = new ToNet(storageToNet);
                    iTransmitterCtrl.prepareStream(iTransmitter);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iTransmitterCtrl);
                addRunnable(iTransmitterCtrl::streamOn);
                break;
            default:
                if (debug) Log.e(TAG, "doInBackground default dataSource");
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