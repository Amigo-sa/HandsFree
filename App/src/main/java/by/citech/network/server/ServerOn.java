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

    private static final String TAG = Tags.SRV_SRVON;
    private static final boolean debug = Settings.debug;

    private IServerCtrlReg iServerCtrlReg;
    private Handler handler;
    private IServerCtrl iServerCtrl;

    public ServerOn(IServerCtrlReg iServerCtrlReg, Handler handler) {
        this.iServerCtrlReg = iServerCtrlReg;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... port) {
        if (debug) Log.i(TAG, "doInBackground");
        int portNum = Integer.parseInt(port[0]);
        if (debug) Log.i(TAG, String.format("%d", portNum));
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
        if (debug) Log.i(TAG, "onProgressUpdate");
        iServerCtrlReg.serverStarted(iServerCtrl[0]);
    }

}
