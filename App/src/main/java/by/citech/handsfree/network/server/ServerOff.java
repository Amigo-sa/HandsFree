package by.citech.handsfree.network.server;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.param.Settings;
import by.citech.handsfree.param.Tags;

public class ServerOff
        extends AsyncTask<IServerCtrl, Void, Void> {

    private static final String TAG = Tags.SRV_SRVOFF;
    private static final boolean debug = Settings.debug;

    private IServerOff iServerOff;

    public ServerOff(IServerOff iServerOff) {
        this.iServerOff = iServerOff;
    }

    @Override
    protected Void doInBackground(IServerCtrl... iServerCtrl) {
        if (debug) Log.i(TAG, "doInBackground");
        if (iServerCtrl[0].isAliveServer()) {
            iServerCtrl[0].stopServer();
            while (iServerCtrl[0].isAliveServer()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (debug) Log.i(TAG, "doInBackground server stopped");
        } else {
            if (debug) Log.i(TAG, "doInBackground server already stopped");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (debug) Log.i(TAG, "onPostExecute");
        iServerOff.serverStopped();
    }

}