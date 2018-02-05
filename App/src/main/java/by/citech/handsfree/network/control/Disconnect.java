package by.citech.handsfree.network.control;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

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
        Timber.i("doInBackground");
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
                Timber.i("doInBackground connection closed");
                return null;
            }
            iConnCtrl[0].closeConnectionForce();
            Timber.w("doInBackground connection force close");
        }
        Timber.i("doInBackground connection already closed");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(TAG, "onPostExecute");
        iDisc.disconnected();
    }

}
