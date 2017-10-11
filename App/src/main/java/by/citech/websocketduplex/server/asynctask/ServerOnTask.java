package by.citech.websocketduplex.server.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import by.citech.websocketduplex.ServerActivity;
import by.citech.websocketduplex.server.network.NanoWebSocketServerCtrl;
import by.citech.websocketduplex.server.network.websockets.NanoWSD;
import by.citech.websocketduplex.util.OpMode;
import by.citech.websocketduplex.util.StatusMessages;
import by.citech.websocketduplex.util.Tags;

public class ServerOnTask extends AsyncTask<String, String, Void> {
    private ServerActivity activity;
    private Handler handler;
    private NanoWebSocketServerCtrl serverCtrl;

    public ServerOnTask(ServerActivity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... port) {
        Log.i(Tags.CLT_TASK_SRVON, "doInBackground");
        int portNum = Integer.parseInt(port[0]);
        Log.i(Tags.CLT_TASK_SRVON, String.format("%d", portNum));
        serverCtrl = new NanoWebSocketServerCtrl(portNum, port[1].equals(OpMode.SRV_DEBUG), handler);

        if (!serverCtrl.isAlive()) {
            try {
                serverCtrl.start(50000);
            } catch (IOException e) {
                publishProgress(StatusMessages.SRV_CANTSTART);
                Log.i(Tags.ACT_SRV, "cant start server, IOException");
            }
        }

        while (!serverCtrl.isAlive()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                publishProgress(StatusMessages.SRV_CANTSTART);
                Log.i(Tags.ACT_SRV, "cant start server, InterruptedException");
            }
        }

        publishProgress(StatusMessages.SRV_STARTED);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        Log.i(Tags.CLT_TASK_SRVON, "onProgressUpdate");
        switch (status[0]) {
            case StatusMessages.SRV_CANTSTART:
                activity.textViewSrvStatus.setText("Состояние: не удалось запустить сервер.");
                activity.btnSrvOn.setEnabled(true);
                break;
            case StatusMessages.SRV_STARTED:
                activity.textViewSrvStatus.setText("Состояние: сервер включен.");
                activity.serverCtrl = serverCtrl;
                activity.btnSrvOff.setEnabled(true);
        }
    }
}
