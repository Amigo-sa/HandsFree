package by.citech.client.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.client.network.IMessage;
import by.citech.client.network.IClientCtrl;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class SendMessageToServerTask extends AsyncTask<String, String, Void> {
    private IMessage iMessage;
    private IClientCtrl iClientCtrl;

    public SendMessageToServerTask (IMessage iMessage, IClientCtrl iClientCtrl) {
        this.iMessage = iMessage;
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    protected Void doInBackground(String... message) {
        if (Settings.debug) Log.i(Tags.CLT_TASK_SEND, "doInBackground");
        if (Settings.debug) Log.i(Tags.CLT_TASK_SEND, String.format("doInBackground message is <%s>", message[0]));
        if (iClientCtrl != null) {
            iClientCtrl.sendMessage(message[0]);
            publishProgress(StatusMessages.CLT_MESSAGE_SENDED);
        } else {
            if (Settings.debug) Log.i(Tags.CLT_TASK_SEND, "doInBackground iClientCtrl is null");
            publishProgress(StatusMessages.CLT_MESSAGE_CANT);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        if (Settings.debug) Log.i(Tags.CLT_TASK_SEND, "onProgressUpdate");
        switch (status[0]) {
            case StatusMessages.CLT_MESSAGE_CANT:
                iMessage.messageCantSend();
            case StatusMessages.CLT_MESSAGE_SENDED:
                iMessage.messageSended();
        }
    }
}