package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

import by.citech.param.Settings;
import by.citech.server.network.IServerOn;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.NanoWebSocketServerCtrl;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class ServerOnTask extends AsyncTask<String, String, Void> {
    private IServerOn iServerOn;
    private Handler handler;
    private IServerCtrl serverCtrl;

    public ServerOnTask(IServerOn iServerOn, Handler handler) {
        this.iServerOn = iServerOn;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... port) {
        Log.i(Tags.SRV_TASK_SRVON, "doInBackground");
        int portNum = Integer.parseInt(port[0]);
        Log.i(Tags.SRV_TASK_SRVON, String.format("%d", portNum));
        serverCtrl = new NanoWebSocketServerCtrl(portNum, handler);

        if (!serverCtrl.isAliveServer()) {
            try {
                serverCtrl.startServer(Settings.serverTimeout);
            } catch (IOException e) {
                publishProgress(StatusMessages.SRV_CANTSTART);
                Log.i(Tags.ACT_SRV, "cant start server, IOException");
            }
        }

        while (!serverCtrl.isAliveServer()) {
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
        Log.i(Tags.SRV_TASK_SRVON, "onProgressUpdate");
        switch (status[0]) {
            case StatusMessages.SRV_CANTSTART:
                iServerOn.serverCantStart();
                break;
            case StatusMessages.SRV_STARTED:
                iServerOn.serverStarted(serverCtrl);
        }
    }
}
