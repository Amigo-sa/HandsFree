package by.citech.network.server;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.network.server.IServerCtrl;
import by.citech.network.server.IServerOff;
import by.citech.param.Tags;

public class ServerOff
        extends AsyncTask<IServerCtrl, Void, Void> {

    private IServerOff iServerOff;

    public ServerOff(IServerOff iServerOff) {
        this.iServerOff = iServerOff;
    }

    @Override
    protected Void doInBackground(IServerCtrl... iServerCtrl) {
        Log.i(Tags.SRV_SRVOFF, "doInBackground");
        if (iServerCtrl[0].isAliveServer()) {
            iServerCtrl[0].stopServer();
            while (iServerCtrl[0].isAliveServer()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(Tags.SRV_SRVOFF, "doInBackground server stopped");
        } else {
            Log.i(Tags.SRV_SRVOFF, "doInBackground server already stopped");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.SRV_SRVOFF, "onPostExecute");
        iServerOff.serverStopped();
    }

}