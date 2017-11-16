package by.citech.network.control.receive;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Redirect extends AsyncTask<String, IRedirectCtrl, Void> {
    private static final String TAG = Tags.NET_REDIR;
    private static final boolean debug = Settings.debug;
    private IRedirectCtrlReg iRedirectCtrlReg;
    private IReceiveListenerReg iReceiveListenerReg;
    private StorageData<byte[][]> storageNetToBt;

    public Redirect(IRedirectCtrlReg iRedirectCtrlReg, IReceiveListenerReg iReceiveListenerReg, StorageData<byte[][]> storageNetToBt) {
        if (debug) Log.i(TAG, "Redirect");
        this.iRedirectCtrlReg = iRedirectCtrlReg;
        this.iReceiveListenerReg = iReceiveListenerReg;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (debug) Log.i(TAG, "doInBackground");
        switch (Settings.dataSource) {
            case MICROPHONE:
                if (debug) Log.i(TAG, "doInBackground audio");
                final RedirectToAudio redirectToAudio = new RedirectToAudio(iReceiveListenerReg);
                publishProgress(redirectToAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (debug) Log.i(TAG, "doInBackground audio startClient in new thread");
                        redirectToAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                Log.i(TAG, "doInBackground bluetooth");
                if (storageNetToBt == null) {
                    if (debug) Log.e(TAG, "doInBackground bluetooth storage is null");
                    return null;
                }
                final RedirectToBluetooth redirectToBluetooth = new RedirectToBluetooth(iReceiveListenerReg, storageNetToBt);
                publishProgress(redirectToBluetooth.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (debug) Log.i(TAG, "doInBackground bluetooth startClient in new thread");
                        redirectToBluetooth.run();
                    }
                }).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IRedirectCtrl... iRedirectCtrl) {
        if (debug) Log.i(TAG, "onProgressUpdate");
        iRedirectCtrlReg.registerRedirectCtrl(iRedirectCtrl[0]);
    }
}
