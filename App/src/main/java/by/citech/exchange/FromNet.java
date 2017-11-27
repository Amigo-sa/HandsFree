package by.citech.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class FromNet
        extends AsyncTask<String, IFromNetCtrl, Void> {

    private static final String TAG = Tags.NET_REDIR;
    private static final boolean debug = Settings.debug;
    private IFromNetCtrlReg iFromNetCtrlReg;
    private IReceiveListenerReg iReceiveListenerReg;
    private StorageData<byte[][]> storageNetToBt;

    public FromNet(IFromNetCtrlReg iFromNetCtrlReg, IReceiveListenerReg iReceiveListenerReg, StorageData<byte[][]> storageNetToBt) {
        if (debug) Log.i(TAG, "FromNet");
        this.iFromNetCtrlReg = iFromNetCtrlReg;
        this.iReceiveListenerReg = iReceiveListenerReg;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                final FromNetToAudio fromNetToAudio = new FromNetToAudio(iReceiveListenerReg);
                publishProgress(fromNetToAudio.start());
                new Thread(() -> {
                    if (debug) Log.i(TAG, "doInBackground audio in new thread");
                    fromNetToAudio.run();
                }).start();
                break;
            case BLUETOOTH:
                Log.i(TAG, "doInBackground bluetooth");
                if (storageNetToBt == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                final FromNetToBluetooth fromNetToBluetooth = new FromNetToBluetooth(iReceiveListenerReg, storageNetToBt);
                publishProgress(fromNetToBluetooth.start());
                new Thread(() -> {
                    if (debug) Log.i(TAG, "doInBackground bluetooth in new thread");
                    fromNetToBluetooth.run();
                }).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IFromNetCtrl... iFromNetCtrl) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iFromNetCtrlReg.registerRedirectCtrl(iFromNetCtrl[0]);
    }

}
