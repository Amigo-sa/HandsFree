package by.citech.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.DataSource;
import by.citech.param.Settings;
import by.citech.server.network.IRedirectCtrl;
import by.citech.server.network.IRedirectOn;
import by.citech.server.network.IServerCtrl;
import by.citech.param.Tags;

public class RedirectDataTask extends AsyncTask<String, IRedirectCtrl, Void> {
    private IRedirectOn iRedirectOn;
    private IServerCtrl iServerCtrl;
    private DataSource dataSource;
    private StorageData storageNetToBt;

    public RedirectDataTask(IRedirectOn iRedirectOn, IServerCtrl iServerCtrl, DataSource dataSource) {
        this.iRedirectOn = iRedirectOn;
        this.iServerCtrl = iServerCtrl;
        this.dataSource = dataSource;
    }

    public RedirectDataTask(IRedirectOn iRedirectOn, IServerCtrl iServerCtrl, DataSource dataSource, StorageData storageNetToBt) {
        this.iRedirectOn = iRedirectOn;
        this.iServerCtrl = iServerCtrl;
        this.dataSource = dataSource;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "doInBackground");
        switch (dataSource) {
            case MICROPHONE:
                Log.i(Tags.SRV_TASK_REDIR, "doInBackground redirect to audio");
                RedirectToAudio redirectToAudio = new RedirectToAudio(iServerCtrl, Integer.parseInt(params[0]));
                publishProgress(redirectToAudio.start());
                redirectToAudio.run();
                break;
            case BLUETOOTH:
                Log.i(Tags.SRV_TASK_REDIR, "doInBackground redirect to bluetooth");
                RedirectToBluetooth redirectToBluetooth = new RedirectToBluetooth(iServerCtrl, Settings.bufferSize, storageNetToBt);
                publishProgress(redirectToBluetooth.start());
                redirectToBluetooth.run();
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
