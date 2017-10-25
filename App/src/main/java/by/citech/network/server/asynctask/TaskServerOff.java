package by.citech.network.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.network.server.connection.IServerCtrl;
import by.citech.network.server.connection.IServerOff;
import by.citech.param.Tags;

public class TaskServerOff extends AsyncTask<IServerCtrl, Void, Void> {
    private IServerOff iServerOff;

    public TaskServerOff(IServerOff iServerOff) {
        this.iServerOff = iServerOff;
    }

    @Override
    protected Void doInBackground(IServerCtrl... iServerCtrl) {
        Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground");
        if (iServerCtrl[0].isAliveServer()) {
            iServerCtrl[0].stopServer();
            while (iServerCtrl[0].isAliveServer()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground server stopped");
        } else {
            Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground server already stopped");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.SRV_TASK_SRVOFF, "onPostExecute");
        iServerOff.serverStopped();
    }
}