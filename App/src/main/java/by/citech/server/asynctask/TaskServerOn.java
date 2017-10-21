package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

import by.citech.param.Settings;
import by.citech.server.network.IServerCtrlRegister;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.ServerCtrlNanoWebSocket;
import by.citech.param.Tags;

public class TaskServerOn extends AsyncTask<String, IServerCtrl, Void> {
    private IServerCtrlRegister iServerCtrlRegister;
    private Handler handler;
    private IServerCtrl iServerCtrl;

    public TaskServerOn(IServerCtrlRegister iServerCtrlRegister, Handler handler) {
        this.iServerCtrlRegister = iServerCtrlRegister;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... port) {
        Log.i(Tags.SRV_TASK_SRVON, "doInBackground");
        int portNum = Integer.parseInt(port[0]);
        Log.i(Tags.SRV_TASK_SRVON, String.format("%d", portNum));
        iServerCtrl = new ServerCtrlNanoWebSocket(portNum, handler);

        if (!iServerCtrl.isAliveServer()) {
            try {
                iServerCtrl.startServer(Settings.serverTimeout);
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
        Log.i(Tags.SRV_TASK_SRVON, "onProgressUpdate");
        iServerCtrlRegister.serverStarted(iServerCtrl[0]);
    }
}
