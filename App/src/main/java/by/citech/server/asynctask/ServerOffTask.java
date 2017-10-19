package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.server.network.IServerCtrl;
import by.citech.server.network.IServerOff;
import by.citech.server.network.websockets.WebSocket;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class ServerOffTask extends AsyncTask<Void, Void, Void> {
    private IServerOff iServerOff;
    private IServerCtrl serverCtrl;

    public ServerOffTask(IServerOff iServerOff, IServerCtrl serverCtrl) throws Exception {
        if (serverCtrl == null) {
            throw new Exception("serverCtrl is null");
        }

        this.iServerOff = iServerOff;
        this.serverCtrl = serverCtrl;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.i(Tags.SRV_TASK_SRVOFF, "doInBackground");
        WebSocket webSocket = serverCtrl.getWebSocket();

        if (webSocket != null && webSocket.isOpen()) {
            serverCtrl.closeSocket();
//          while (webSocket.isOpen()) {
            while (!serverCtrl.getStatus().equals(StatusMessages.WEBSOCKET_CLOSED)
                    && !serverCtrl.getStatus().equals(StatusMessages.WEBSOCKET_FAILURE)) {
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

        if (serverCtrl.isAliveServer()) {
            serverCtrl.stopServer();

            while (serverCtrl.isAliveServer()) {
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