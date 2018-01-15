package by.citech.handsfree.exchange;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public class SendMessage
        extends AsyncTask<String, String, Void> {

    private IMessageResult iMessageResult;
    private ITransmitter iTransmitter;

    public SendMessage(IMessageResult iMessageResult, ITransmitter iTransmitter) {
        this.iMessageResult = iMessageResult;
        this.iTransmitter = iTransmitter;
    }

    @Override
    protected Void doInBackground(String... message) {
        if (Settings.debug) Log.i(Tags.SendMessage, "doInBackground");
        if (Settings.debug) Log.i(Tags.SendMessage, String.format("doInBackground message is <%s>", message[0]));
        if (iTransmitter != null) {
            iTransmitter.sendMessage(message[0]);
            publishProgress(StatusMessages.CLT_MESSAGE_SENDED);
        } else {
            if (Settings.debug) Log.i(Tags.SendMessage, "doInBackground iClientCtrl is null");
            publishProgress(StatusMessages.CLT_MESSAGE_CANT);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        if (Settings.debug) Log.i(Tags.SendMessage, "onProgressUpdate");
        switch (status[0]) {
            case StatusMessages.CLT_MESSAGE_CANT:
                iMessageResult.messageCantSend();
            case StatusMessages.CLT_MESSAGE_SENDED:
                iMessageResult.messageSended();
        }
    }

}