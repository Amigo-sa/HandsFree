package by.citech.websocketduplex.server.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import by.citech.websocketduplex.ServerActivity;
import by.citech.websocketduplex.param.DataSource;
import by.citech.websocketduplex.param.Settings;
import by.citech.websocketduplex.server.network.IRedirect;
import by.citech.websocketduplex.server.network.NanoWebSocketServerCtrl;
import by.citech.websocketduplex.server.network.RedirectToAudio;
import by.citech.websocketduplex.param.Tags;

public class RedirectDataTask extends AsyncTask<String, IRedirect, Void> {
    private ServerActivity activity;
    private NanoWebSocketServerCtrl serverCtrl;
    private DataSource dataSource;

    public RedirectDataTask(ServerActivity activity, NanoWebSocketServerCtrl serverCtrl, DataSource dataSource) {
        this.activity = activity;
        this.serverCtrl = serverCtrl;
        this.dataSource = dataSource;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "doInBackground");

        switch (dataSource) {
            case MICROPHONE:
                Log.i(Tags.SRV_TASK_REDIR, "doInBackground redirect to audio");
                RedirectToAudio redirectToAudio = new RedirectToAudio(serverCtrl, Integer.parseInt(params[0]));
                publishProgress(redirectToAudio.start());
                redirectToAudio.run();
                break;
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(IRedirect... iRedirect) {
        if (Settings.debug) Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate");

        if (iRedirect[0] != null) {
            activity.iRedirect = iRedirect[0];
            Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate iRedirect is not null");
        } else {
            Log.i(Tags.SRV_TASK_REDIR, "onProgressUpdate iRedirect is null");
        }
    }
}
