package by.citech.network.receive;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToAudio
        implements IRedirectCtrl, IReceiveListener {

    private int bufferSize = Settings.bufferSize;
    private IReceiveListenerReg iReceiveListenerReg;
    private AudioTrack audioTrack;
    private boolean isRedirecting = false;

    RedirectToAudio(IReceiveListenerReg iReceiveListenerReg) {
        this.iReceiveListenerReg = iReceiveListenerReg;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "build");
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

        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "build done");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "startClient");
        isRedirecting = true;
        audioTrack.play();
        iReceiveListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "startClient done");
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "onReceiveData");
        if (isRedirecting) {
            audioTrack.write(data, 0, bufferSize);
        }
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "redirectOff");
        isRedirecting = false;
        iReceiveListenerReg.registerReceiverListener(null);
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }
            audioTrack.release();
            audioTrack = null;
        }
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "redirectOff done");
    }

}
