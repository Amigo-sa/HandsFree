package by.citech.websocketduplex.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.websocketduplex.ServerActivity;
import by.citech.websocketduplex.server.network.NanoWebSocketServerCtrl;
import by.citech.websocketduplex.server.network.websockets.WebSocket;
import by.citech.websocketduplex.param.StatusMessages;
import by.citech.websocketduplex.param.Tags;

public class ServerOffTask extends AsyncTask<Void, Void, Void> {
    private ServerActivity activity;
    private NanoWebSocketServerCtrl serverCtrl;

    public ServerOffTask(ServerActivity activity, NanoWebSocketServerCtrl serverCtrl) throws Exception {
        if (serverCtrl == null) {
            throw new Exception("serverCtrl is null");
        }

        this.activity = activity;
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

        if (serverCtrl.isAlive()) {
            serverCtrl.stop();

            while (serverCtrl.isAlive()) {
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
        activity.btnSrvOff.setEnabled(false);
        activity.btnSrvOn.setEnabled(true);
        activity.textViewSrvStatus.setText("Состояние: сервер выключен.");
    }
}