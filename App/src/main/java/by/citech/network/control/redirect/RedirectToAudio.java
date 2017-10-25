package by.citech.network.control.redirect;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import by.citech.network.control.IReceiverListener;
import by.citech.network.control.IReceiverListenerReg;
import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToAudio implements IRedirectCtrl, IReceiverListener {
    private int bufferSize;
    private IReceiverListenerReg iReceiverListenerReg;
    private AudioTrack audioTrack;
    private boolean isRedirecting = false;

    RedirectToAudio(IReceiverListenerReg iReceiverListenerReg, int bufferSize) {
        this.iReceiverListenerReg = iReceiverListenerReg;
        this.bufferSize = bufferSize;
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "start");
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

        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "start done");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "startClient");
        isRedirecting = true;
        audioTrack.play();
        iReceiverListenerReg.registerReceiverListener(this);
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "startClient done");
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "onReceiveMessage");
        if (isRedirecting) {
            audioTrack.write(data, 0, bufferSize);
        }
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "redirectOff");
        isRedirecting = false;
        iReceiverListenerReg.registerReceiverListener(null);
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
