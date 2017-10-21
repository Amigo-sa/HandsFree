package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.client.network.IClientOff;
import by.citech.client.network.IClientCtrl;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class TaskDisconnect extends AsyncTask<IClientCtrl, String, Void> {
    private static final int TIMEOUT_PERIOD = 250;
    private static final int TIMEOUT_CYCLES = 10;
    private IClientOff iClientOff;

    public TaskDisconnect(IClientOff iClientOff) {
        this.iClientOff = iClientOff;
    }

    @Override
    protected Void doInBackground(IClientCtrl... clientCtrl) {
        Log.i(Tags.CLT_TASK_DISC, "doInBackground");

        if (clientCtrl[0] == null) {
            Log.e(Tags.CLT_TASK_DISC, "doInBackground iClientCtrl is null");
            publishProgress(StatusMessages.WEBSOCKET_CLOSED);
            return null;
        }

        clientCtrl[0].stop("user manually closed connection");
        int i = 0;

        while (i < TIMEOUT_CYCLES) {
            i++;
            String status = clientCtrl[0].getStatus();

            if (status.equals(StatusMessages.WEBSOCKET_CLOSED)) {
                publishProgress(status);
                return null;
            }

            try {
                Thread.sleep(TIMEOUT_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        clientCtrl[0].cancel();
        publishProgress(StatusMessages.WEBSOCKET_CANCEL);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        Log.i(Tags.CLT_TASK_DISC, "onProgressUpdate");
        iClientOff.clientStopped(status[0]);
    }
}
