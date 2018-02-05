package by.citech.handsfree.network.server;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class ServerOff
        extends AsyncTask<IServerCtrl, Void, Void> {

    private static final String TAG = Tags.ServerOff;
    private static final boolean debug = Settings.debug;

    private IServerOff iServerOff;

    public ServerOff(IServerOff iServerOff) {
        this.iServerOff = iServerOff;
    }

    @Override
    protected Void doInBackground(IServerCtrl... iServerCtrl) {
        Timber.i("doInBackground");
        if (iServerCtrl[0].isAliveServer()) {
            iServerCtrl[0].stopServer();
            while (iServerCtrl[0].isAliveServer()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Timber.i("doInBackground server stopped");
        } else {
            Timber.i("doInBackground server already stopped");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Timber.i("onPostExecute");
        iServerOff.onServerStop();
    }

}