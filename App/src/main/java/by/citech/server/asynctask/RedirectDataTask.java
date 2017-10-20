package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.connection.IReceiver;
import by.citech.connection.IReceiverRegister;
import by.citech.data.StorageData;
import by.citech.param.DataSource;
import by.citech.param.Settings;
import by.citech.server.network.IRedirectCtrl;
import by.citech.server.network.IRedirectOn;
import by.citech.server.network.IServerCtrl;
import by.citech.param.Tags;

public class RedirectDataTask extends AsyncTask<String, IRedirectCtrl, Void> {
    private IRedirectOn iRedirectOn;
    private IReceiverRegister iReceiverRegister;
    private DataSource dataSource;
    private StorageData storageNetToBt;

    public RedirectDataTask(IRedirectOn iRedirectOn, IReceiverRegister iReceiverRegister, DataSource dataSource) {
        this.iRedirectOn = iRedirectOn;
        this.iReceiverRegister = iReceiverRegister;
        this.dataSource = dataSource;
    }

    public RedirectDataTask(IRedirectOn iRedirectOn, IReceiverRegister iReceiverRegister, DataSource dataSource, StorageData storageNetToBt) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "RedirectDataTask");
        this.iRedirectOn = iRedirectOn;
        this.iReceiverRegister = iReceiverRegister;
        this.dataSource = dataSource;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "doInBackground");
        switch (dataSource) {
            case MICROPHONE:
                Log.i(Tags.SRV_TASK_REDIR, "doInBackground redirect to audio");
                final RedirectToAudio redirectToAudio = new RedirectToAudio(iReceiverRegister, Integer.parseInt(params[0]));
                publishProgress(redirectToAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        redirectToAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                Log.i(Tags.SRV_TASK_REDIR, "doInBackground redirect to bluetooth");
                RedirectToBluetooth redirectToBluetooth = new RedirectToBluetooth(iReceiverRegister, Settings.bufferSize, storageNetToBt);
                publishProgress(redirectToBluetooth.start());
                redirectToBluetooth.run();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IRedirectCtrl... iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate");
        if (iRedirectCtrl[0] != null) {
            iRedirectOn.setRedirect(iRedirectCtrl[0]);
            Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate iRedirectCtrl is not null");
        } else {
            Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate iRedirectCtrl is null");
        }
    }
}
