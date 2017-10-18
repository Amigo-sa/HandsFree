package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.param.StatusMessages;
import by.citech.websocketduplex.param.Tags;

public class DisconnectTask extends AsyncTask<OkWebSocketClientCtrl, String, Void> {
    private static final int TIMEOUT_PERIOD = 500;
    private static final int TIMEOUT_CYCLES = 20;
    private ClientActivity activity;

    public DisconnectTask (ClientActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(OkWebSocketClientCtrl... clientCtrl) {
        Log.i(Tags.CLT_TASK_DISC, "doInBackground");

        if (clientCtrl[0] == null) {
            Log.i(Tags.CLT_TASK_DISC, "doInBackground clientCtrl is null");
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
                activity.textViewCltStatus.setText("Состояние: соединение завершено корректно.");
                break;
            case StatusMessages.WEBSOCKET_CANCEL:
                activity.textViewCltStatus.setText("Состояние: соединение завершено принудительно.");
                break;
            default:
                activity.textViewCltStatus.setText("Состояние: неизвестный статус.");
        }

        activity.editTextCltToSrvText.setVisibility(View.INVISIBLE);
        activity.btnCltSendMsg.setEnabled(false);
        activity.btnCltStreamOn.setEnabled(false);
        activity.btnCltStreamOff.setEnabled(false);
        activity.btnCltConnToSrv.setEnabled(true);
        activity.clientCtrl = null;
        activity.btnCltConnToSrv.setEnabled(true);
    }
}
