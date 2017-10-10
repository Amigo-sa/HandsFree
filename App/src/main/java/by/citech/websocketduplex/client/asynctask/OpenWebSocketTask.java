package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.websocket.OkWebSocketClientCtrl;
import by.citech.websocketduplex.utils.StatusMessages;
import by.citech.websocketduplex.utils.Tags;

public class OpenWebSocketTask extends AsyncTask<String, String, Void> {
    private static final int TIMEOUT_PERIOD = 100;
    private static final int TIMEOUT_CYCLES = 1000;

    private ClientActivity activity;
    private OkWebSocketClientCtrl clientCtrl;

    public OpenWebSocketTask (ClientActivity activity) {
        this.activity = activity;
    }

    protected Void doInBackground(String... url) {
        Log.i(Tags.TASK_OWS, "OpenWebSocketTask doInBackground");
        Log.i(Tags.TASK_OWS, url[0]);
        clientCtrl = new OkWebSocketClientCtrl(url[0]);
        clientCtrl.run();
        int i = 0;

        while (i < TIMEOUT_CYCLES) {
            i++;
            String status = clientCtrl.getStatus();

            if (!status.equals("")) {
                publishProgress(status);
                return null;
            }

            try {
                Thread.sleep(TIMEOUT_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        clientCtrl.stop("Waiting for YOU, dear SERVER, for too long!");
        publishProgress(StatusMessages.WEBSOCKET_TIMEOUT);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        Log.i(Tags.TASK_OWS, "OpenWebSocketTask onProgressUpdate");
        Log.i(Tags.TASK_OWS, status[0]);
        switch (status[0]) {
            case StatusMessages.WEBSOCKET_OPEN:
                activity.textViewCltStatus.setText("Состояние: подключено");
                activity.editTextCltToSrvText.setVisibility(View.VISIBLE);
                activity.editTextCltToSrvText.setText("РОИССЯ ВПЕРДЕ");
                activity.btnCltDiscFromSrv.setEnabled(true);
                activity.btnCltSendMsg.setEnabled(true);
                activity.btnCltStreamOn.setEnabled(true);
                activity.clientCtrl = clientCtrl;
            case StatusMessages.WEBSOCKET_NOTAVAILABLE:
                activity.textViewCltStatus.setText("Состояние: подключение не удалось.");
                break;
            case StatusMessages.WEBSOCKET_FAILURE:
                activity.textViewCltStatus.setText("Состояние: ошибка при подключении.");
                break;
            case StatusMessages.WEBSOCKET_TIMEOUT:
                activity.textViewCltStatus.setText("Состояние: превышен таймаут.");
                break;
            default:
                activity.textViewCltStatus.setText("Состояние: неизвестный статус.");
        }
        activity.btnCltConnToSrv.setEnabled(true);
    }
}