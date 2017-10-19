package by.citech.websocketduplex.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.websocketduplex.client.network.IMessage;
import by.citech.websocketduplex.client.network.IClientCtrl;
import by.citech.websocketduplex.param.Tags;

public class SendMessageToServerTask extends AsyncTask<String, Void, Void> {
    private IMessage iMessage;
    private IClientCtrl iClientCtrl;

    public SendMessageToServerTask (IMessage iMessage, IClientCtrl iClientCtrl) {
        this.iMessage = iMessage;
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    protected Void doInBackground(String... message) {
        Log.i(Tags.CLT_TASK_SMSG, "SendMessageToServerTask doInBackground");
        Log.i(Tags.CLT_TASK_SMSG, message[0]);
        iClientCtrl.sendMessage(message[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.i(Tags.CLT_TASK_SMSG, "SendMessageToServerTask onPostExecute");
        iMessage.messageSended();
    }
}