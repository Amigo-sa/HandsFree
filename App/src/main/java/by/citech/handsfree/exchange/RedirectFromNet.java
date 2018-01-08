package by.citech.handsfree.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.exchange.consumers.ToBluetooth;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreadManager;

public class RedirectFromNet
        extends AsyncTask<EDataSource, ITransmitterCtrl, Void>
        implements IThreadManager {

    private static final String TAG = Tags.RedirectFromNet;
    private static final boolean debug = Settings.debug;

    private ITransmitterCtrlReg iTransmitterCtrlReg;
    private IExchangeCtrl iExchangeCtrl;
    private StorageData<byte[][]> storageFromNet;

    public RedirectFromNet(ITransmitterCtrlReg iTransmitterCtrlReg, IExchangeCtrl iExchangeCtrl, StorageData<byte[][]> storageFromNet) {
        this.iTransmitterCtrlReg = iTransmitterCtrlReg;
        this.iExchangeCtrl = iExchangeCtrl;
        this.storageFromNet = storageFromNet;
    }

    @Override
    protected Void doInBackground(EDataSource... params) {
        if (debug) Log.i(TAG, "doInBackground");
        ITransmitterCtrl iTransmitterCtrl;
        switch (params[0]) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                try {
                    ToAudioOut toAudioOut = new ToAudioOut();
                    iExchangeCtrl.setReceiver(toAudioOut);
                    iTransmitterCtrl = toAudioOut;
                    iTransmitterCtrl.prepareStream(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iTransmitterCtrl);
                addRunnable(iTransmitterCtrl::streamOn);
                break;
            case BLUETOOTH:
                Log.i(TAG, "doInBackground bluetooth");
                try {
                    ToBluetooth toBluetooth = new ToBluetooth(storageFromNet);
                    iExchangeCtrl.setReceiver(toBluetooth);
                    iTransmitterCtrl = toBluetooth;
                    iTransmitterCtrl.prepareStream(null);
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
    protected void onProgressUpdate(ITransmitterCtrl... iTransmitterCtrls) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iTransmitterCtrlReg.registerTransmitterCtrl(iTransmitterCtrls[0]);
    }

}
