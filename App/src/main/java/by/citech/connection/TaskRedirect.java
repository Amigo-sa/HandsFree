package by.citech.connection;

import android.os.AsyncTask;
import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.DataSource;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class TaskRedirect extends AsyncTask<String, IRedirectCtrl, Void> {
    private IRedirectCtrlReg iRedirectCtrlReg;
    private IReceiverListenerReg iReceiverListenerReg;
    private DataSource dataSource;
    private StorageData storageNetToBt;

    public TaskRedirect(IRedirectCtrlReg iRedirectCtrlReg, IReceiverListenerReg iReceiverListenerReg, DataSource dataSource) {
        this.iRedirectCtrlReg = iRedirectCtrlReg;
        this.iReceiverListenerReg = iReceiverListenerReg;
        this.dataSource = dataSource;
    }

    public TaskRedirect(IRedirectCtrlReg iRedirectCtrlReg, IReceiverListenerReg iReceiverListenerReg, DataSource dataSource, StorageData storageNetToBt) {
        if (Settings.debug) Log.i(Tags.NET_TASK_REDIR, "TaskRedirect");
        this.iRedirectCtrlReg = iRedirectCtrlReg;
        this.iReceiverListenerReg = iReceiverListenerReg;
        this.dataSource = dataSource;
        this.storageNetToBt = storageNetToBt;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (Settings.debug) Log.i(Tags.NET_TASK_REDIR, "doInBackground");
        switch (dataSource) {
            case MICROPHONE:
                Log.i(Tags.NET_TASK_REDIR, "doInBackground redirect to audio");
//              final RedirectToAudio redirectToAudio = new RedirectToAudio(iReceiverListenerReg, Integer.parseInt(params[0]));
                final RedirectToAudio redirectToAudio = new RedirectToAudio(iReceiverListenerReg, Settings.bufferSize);
                publishProgress(redirectToAudio.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Tags.NET_TASK_REDIR, "doInBackground redirect to audio startClient in new thread");
                        redirectToAudio.run();
                    }
                }).start();
                break;
            case BLUETOOTH:
                Log.i(Tags.NET_TASK_REDIR, "doInBackground redirect to bluetooth");
                final RedirectToBluetooth redirectToBluetooth = new RedirectToBluetooth(iReceiverListenerReg, Settings.bufferSize, storageNetToBt);
                publishProgress(redirectToBluetooth.start());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Tags.NET_TASK_REDIR, "doInBackground redirect to bluetooth startClient in new thread");
                        redirectToBluetooth.run();
                    }
                }).start();
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(IRedirectCtrl... iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.NET_TASK_REDIR, "onProgressUpdate");
        iRedirectCtrlReg.registerRedirectCtrl(iRedirectCtrl[0]);
    }
}
