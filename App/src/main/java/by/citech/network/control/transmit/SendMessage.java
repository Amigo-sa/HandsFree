package by.citech.network.control.transmit;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.network.control.IMessage;
import by.citech.network.control.ITransmitter;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class SendMessage extends AsyncTask<String, String, Void> {
    private IMessage iMessage;
    private ITransmitter iTransmitter;

    public SendMessage(IMessage iMessage, ITransmitter iTransmitter) {
        this.iMessage = iMessage;
        this.iTransmitter = iTransmitter;
    }

    @Override
    protected Void doInBackground(String... message) {
        if (Settings.debug) Log.i(Tags.NET_SEND, "doInBackground");
        if (Settings.debug) Log.i(Tags.NET_SEND, String.format("doInBackground message is <%s>", message[0]));
        if (iTransmitter != null) {
            iTransmitter.sendMessage(message[0]);
            publishProgress(StatusMessages.CLT_MESSAGE_SENDED);
        } else {
            if (Settings.debug) Log.i(Tags.NET_SEND, "doInBackground iClientCtrl is null");
            publishProgress(StatusMessages.CLT_MESSAGE_CANT);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        if (Settings.debug) Log.i(Tags.NET_SEND, "onProgressUpdate");
        switch (status[0]) {
            case StatusMessages.CLT_MESSAGE_CANT:
                iMessage.messageCantSend();
            case StatusMessages.CLT_MESSAGE_SENDED:
                iMessage.messageSended();
        }
    }
}