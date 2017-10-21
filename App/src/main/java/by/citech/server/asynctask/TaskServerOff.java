package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.server.network.IServerCtrl;
import by.citech.server.network.IServerOff;
import by.citech.server.network.websockets.WebSocket;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class TaskServerOff extends AsyncTask<IServerCtrl, Void, Void> {
    private IServerOff iServerOff;

    public TaskServerOff(IServerOff iServerOff) {
        this.iServerOff = iServerOff;
    }

    @Override
    protected Void doInBackground(IServerCtrl... iServerCtrl) {
        Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground");
        WebSocket webSocket = iServerCtrl[0].getWebSocket();

        if (webSocket != null && webSocket.isOpen()) {
            iServerCtrl[0].closeSocket();
//          while (webSocket.isOpen()) {
            while (!iServerCtrl[0].getStatus().equals(StatusMessages.WEBSOCKET_CLOSED)
                    && !iServerCtrl[0].getStatus().equals(StatusMessages.WEBSOCKET_FAILURE)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground InterruptedException - 1");
                }
            }

            Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground socket closed");
        } else {
            Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground socket null or already closed");
        }

        if (iServerCtrl[0].isAliveServer()) {
            iServerCtrl[0].stopServer();

            while (iServerCtrl[0].isAliveServer()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground InterruptedException - 2");
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
        Log.i(Tags.SRV_TASK_SRVOFF, "onProgressUpdate");
        iServerOff.serverStopped();
    }
}