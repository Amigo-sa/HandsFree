package by.citech.handsfree.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.exchange.consumers.ToBluetooth;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreading;

public class RedirectFromNet
        extends AsyncTask<EDataSource, IStreamer, Void>
        implements IThreading {

    private static final String TAG = Tags.RedirectFromNet;
    private static final boolean debug = Settings.debug;

    private IStreamerRegister iStreamerRegister;
    private IExchangeCtrl iExchangeCtrl;
    private StorageData<byte[][]> storageFromNet;

    public RedirectFromNet(IStreamerRegister iStreamerRegister, IExchangeCtrl iExchangeCtrl, StorageData<byte[][]> storageFromNet) {
        this.iStreamerRegister = iStreamerRegister;
        this.iExchangeCtrl = iExchangeCtrl;
        this.storageFromNet = storageFromNet;
    }

    @Override
    protected Void doInBackground(EDataSource... params) {
        if (debug) Log.i(TAG, "doInBackground");
        IStreamer iStreamer;
        switch (params[0]) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                try {
                    ToAudioOut toAudioOut = new ToAudioOut();
                    iExchangeCtrl.setReceiver(toAudioOut);
                    iStreamer = toAudioOut;
                    iStreamer.prepareStream(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iStreamer);
                addRunnable(iStreamer::streamOn);
                break;
            case BLUETOOTH:
                Log.i(TAG, "doInBackground bluetooth");
                try {
                    ToBluetooth toBluetooth = new ToBluetooth(storageFromNet);
                    iExchangeCtrl.setReceiver(toBluetooth);
                    iStreamer = toBluetooth;
                    iStreamer.prepareStream(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iStreamer);
                addRunnable(iStreamer::streamOn);
                break;
            default:
                if (debug) Log.e(TAG, "doInBackground default dataSource");
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IStreamer... iStreamers) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iStreamerRegister.registerTransmitterCtrl(iStreamers[0]);
    }

}
