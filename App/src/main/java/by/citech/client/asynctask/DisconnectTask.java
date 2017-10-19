package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import by.citech.websocketduplex.client.network.IClientOff;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.client.network.IClientCtrl;
import by.citech.websocketduplex.param.StatusMessages;
import by.citech.websocketduplex.param.Tags;

public class DisconnectTask extends AsyncTask<IClientCtrl, String, Void> {
    private static final int TIMEOUT_PERIOD = 500;
    private static final int TIMEOUT_CYCLES = 20;
    private IClientOff iClientOff;

    public DisconnectTask (IClientOff iClientOff) {
        this.iClientOff = iClientOff;
    }

    @Override
    protected Void doInBackground(IClientCtrl... clientCtrl) {
        Log.i(Tags.CLT_TASK_DISC, "doInBackground");

        if (clientCtrl[0] == null) {
            Log.i(Tags.CLT_TASK_DISC, "doInBackground iClientCtrl is null");
            publishProgress(StatusMessages.WEBSOCKET_CLOSED);
            return null;
        }

        clientCtrl[0].stop("User manually closed connection.");
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
        Log.i(Tags.CLT_TASK_DISC, status[0]);
        switch (status[0]) {
            case StatusMessages.WEBSOCKET_CLOSED:
                iClientOff.clientStopped("соединение завершено корректно");
                break;
            case StatusMessages.WEBSOCKET_CANCEL:
                iClientOff.clientStopped("соединение завершено принудительно");
                break;
        }
    }
}
