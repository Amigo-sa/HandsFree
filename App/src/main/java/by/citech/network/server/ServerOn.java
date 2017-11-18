package by.citech.network.server;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

import by.citech.param.Settings;
import by.citech.network.server.IServerCtrlReg;
import by.citech.network.server.IServerCtrl;
import by.citech.network.server.ServerCtrlNanoWebSocket;
import by.citech.param.Tags;

public class ServerOn
        extends AsyncTask<String, IServerCtrl, Void> {

    private IServerCtrlReg iServerCtrlReg;
    private Handler handler;
    private IServerCtrl iServerCtrl;

    public ServerOn(IServerCtrlReg iServerCtrlReg, Handler handler) {
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
