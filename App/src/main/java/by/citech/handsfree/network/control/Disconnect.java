package by.citech.handsfree.network.control;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class Disconnect
        extends AsyncTask<IConnCtrl, Void, Void> {

    private static final String STAG = Tags.Disconnect;
    private static final boolean debug = Settings.debug;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++; TAG = STAG + " " + objCount;}

    private static final int TIMEOUT_PERIOD = 500;
    private static final int TIMEOUT_CYCLES = 10;
    private IDisc iDisc;

    public Disconnect(IDisc iDisc) {
        this.iDisc = iDisc;
    }

    @Override
    protected Void doInBackground(IConnCtrl... iConnCtrl) {
        if (debug) Log.i(TAG, "doInBackground");
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
                if (debug) Log.i(TAG, "doInBackground connection closed");
                return null;
            }
            iConnCtrl[0].closeConnectionForce();
            if (debug) Log.w(TAG, "doInBackground connection force close");
        }
        if (debug) Log.i(TAG, "doInBackground connection already closed");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(TAG, "onPostExecute");
        iDisc.disconnected();
    }

}
