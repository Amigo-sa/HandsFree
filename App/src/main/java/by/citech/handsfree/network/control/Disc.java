package by.citech.handsfree.network.control;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.handsfree.param.Settings;
import by.citech.handsfree.param.Tags;

public class Disc
        extends AsyncTask<IConnCtrl, Void, Void> {

    private static final int TIMEOUT_PERIOD = 500;
    private static final int TIMEOUT_CYCLES = 10;
    private IDisc iDisc;

    public Disc(IDisc iDisc) {
        this.iDisc = iDisc;
    }

    @Override
    protected Void doInBackground(IConnCtrl... iConnCtrl) {
        if (Settings.debug) Log.i(Tags.NET_DISC, "doInBackground");
        if (iConnCtrl[0].isAliveConnection()) {
            iConnCtrl[0].closeConnection();
            try {
                Thread.sleep(TIMEOUT_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i = 0;
            while (iConnCtrl[0].isAliveConnection() && i < TIMEOUT_CYCLES) {
                i++;
                iConnCtrl[0].closeConnection();
                try {
                    Thread.sleep(TIMEOUT_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!iConnCtrl[0].isAliveConnection()) {
                if (Settings.debug) Log.i(Tags.NET_DISC, "doInBackground connection closed");
                return null;
            }
            iConnCtrl[0].closeConnectionForce();
            if (Settings.debug) Log.e(Tags.NET_DISC, "doInBackground connection closed force");
        }
        if (Settings.debug) Log.i(Tags.NET_DISC, "doInBackground connection already closed");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.NET_DISC, "onPostExecute");
        iDisc.disconnected();
    }

}
