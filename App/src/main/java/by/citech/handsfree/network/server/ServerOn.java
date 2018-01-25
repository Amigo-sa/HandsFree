package by.citech.handsfree.network.server;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ServerOn
        extends AsyncTask<String, IServerCtrl, Void> {

    private static final String TAG = Tags.ServerOn;
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
        int portNum = Integer.parseInt(port[0]);
        if (debug) Log.i(TAG, "doInBackground portnum is " + portNum);
        Server server = new Server(portNum, handler);

        if (!server.isAliveServer()) {
            try {
                iServerCtrl = server.startServer(Settings.Network.serverTimeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (iServerCtrl == null) {
            return null;
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
        iServerCtrlReg.registerServerCtrl(iServerCtrl[0]);
    }

}
