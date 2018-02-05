package by.citech.handsfree.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.consumers.ToNet;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

public class RedirectToNet
        extends AsyncTask<EDataSource, IStreamer, Void>
        implements IThreading {

    private static final String TAG = Tags.RedirectToNet;
    private static final boolean debug = Settings.debug;

    private IStreamerRegister iStreamerRegister;
    private IRxComplex iRxComplex;
    private StorageData<byte[]> storageToNet;

    public RedirectToNet(IStreamerRegister iStreamerRegister, IRxComplex iRxComplex, StorageData<byte[]> storageToNet) {
        this.iStreamerRegister = iStreamerRegister;
        this.iRxComplex = iRxComplex;
        this.storageToNet = storageToNet;
    }

    @Override
    protected Void doInBackground(EDataSource... params) {
        Timber.i("doInBackground");
        IStreamer iStreamer;
        switch (params[0]) {
            case MICROPHONE:
                Timber.i("doInBackground audio");
                iStreamer = new FromAudioIn();
                try {
                    iStreamer.prepareStream(iRxComplex);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iStreamer);
                addRunnable(iStreamer::streamOn);
                break;
            case BLUETOOTH:
                Timber.i("doInBackground bluetooth");
                try {
                    iStreamer = new ToNet(storageToNet);
                    iStreamer.prepareStream(iRxComplex);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                publishProgress(iStreamer);
                addRunnable(iStreamer::streamOn);
                break;
            default:
                Timber.e("doInBackground default dataSource");
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IStreamer... iStreamer) {
        Timber.i("onProgressUpdate");
        iStreamerRegister.registerTransmitterCtrl(iStreamer[0]);
    }

}