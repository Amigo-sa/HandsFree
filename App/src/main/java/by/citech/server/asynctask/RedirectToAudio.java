package by.citech.websocketduplex.server.asynctask;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;
import by.citech.websocketduplex.param.Settings;
import by.citech.websocketduplex.param.Tags;
import by.citech.websocketduplex.server.network.IRedirectCtrl;
import by.citech.websocketduplex.server.network.IServerCtrl;
import by.citech.websocketduplex.server.network.IServerListener;

class RedirectToAudio implements IRedirectCtrl, IServerListener {
    private int bufferSize;
    private IServerCtrl serverCtrl;
    private AudioTrack audioTrack;
    private boolean isRedirecting = false;

    RedirectToAudio(IServerCtrl serverCtrl, int bufferSize) {
        this.serverCtrl = serverCtrl;
        this.bufferSize = bufferSize;
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "redirectOff");
        isRedirecting = false;
        serverCtrl.setListener(null);

        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }

            audioTrack.release();
            audioTrack = null;
        }

        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "redirectOff done");
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "start");
        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, String.format("start audioInBuffersize is %d", bufferSize));
        redirectOff();

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(Settings.audioUsage)
                        .setContentType(Settings.audioContentType)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(Settings.audioEncoding)
                        .setSampleRate(Settings.audioRate)
                        .setChannelMask(Settings.audioOutChannel)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(Settings.audioMode)
                .build();

        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "start done");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "run");
        isRedirecting = true;
        audioTrack.play();
        serverCtrl.setListener(this);
    }

    @Override
    public void onMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.SRV_REDIR_AUDIO, "onMessage");
        if (isRedirecting) {
            audioTrack.write(data, 0, bufferSize);
        }
    }
}
