package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.websocketduplex.ClientActivity;
import by.citech.websocketduplex.client.network.OkWebSocketClientCtrl;
import by.citech.websocketduplex.util.Tags;

public class SendMessageToServerTask extends AsyncTask<String, Void, Void> {
    private ClientActivity activity;
    private OkWebSocketClientCtrl clientCtrl;

    public SendMessageToServerTask (ClientActivity activity, OkWebSocketClientCtrl clientCtrl) {
        this.activity = activity;
        this.clientCtrl = clientCtrl;
    }

    @Override
    protected Void doInBackground(String... message) {
        Log.i(Tags.CLT_TASK_SMSG, "SendMessageToServerTask doInBackground");
        Log.i(Tags.CLT_TASK_SMSG, message[0]);
        clientCtrl.sendMessage(message[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i(Tags.CLT_TASK_SMSG, "SendMessageToServerTask onPostExecute");
        activity.editTextCltToSrvText.setText("");
        activity.btnCltSendMsg.setEnabled(true);
        activity.textViewCltStatus.setText("Состояние: сообщение отправлено");
    }
}