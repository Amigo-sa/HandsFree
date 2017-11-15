package by.citech.network.server.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

import by.citech.param.Settings;
import by.citech.network.server.connection.IServerCtrlReg;
import by.citech.network.server.connection.IServerCtrl;
import by.citech.network.server.connection.ServerCtrlNanoWebSocket;
import by.citech.param.Tags;

public class TaskServerOn extends AsyncTask<String, IServerCtrl, Void> {
    private IServerCtrlReg iServerCtrlReg;
    private Handler handler;
    private IServerCtrl iServerCtrl;

    public TaskServerOn(IServerCtrlReg iServerCtrlReg, Handler handler) {
        this.iServerCtrlReg = iServerCtrlReg;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... port) {
        Log.i(Tags.SRV_SRVON, "doInBackground");
        int portNum = Integer.parseInt(port[0]);
        Log.i(Tags.SRV_SRVON, String.format("%d", portNum));
        ServerCtrlNanoWebSocket serverCtrlNanoWebSocket = new ServerCtrlNanoWebSocket(portNum, handler);

        if (!serverCtrlNanoWebSocket.isAliveServer()) {
            try {
                iServerCtrl = serverCtrlNanoWebSocket.startServer(Settings.serverTimeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!iServerCtrl.isAliveServer()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        publishProgress(iServerCtrl);
        return null;
    }

    @Override
    protected void onProgressUpdate(IServerCtrl... iServerCtrl) {
        Log.i(Tags.SRV_SRVON, "onProgressUpdate");
        iServerCtrlReg.serverStarted(iServerCtrl[0]);
    }
}
