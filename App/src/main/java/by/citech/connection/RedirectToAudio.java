package by.citech.connection;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;

class RedirectToAudio implements IRedirectCtrl, IReceiverListener {
    private int bufferSize;
    private IReceiverListenerRegister iReceiverListenerRegister;
    private AudioTrack audioTrack;
    private boolean isRedirecting = false;

    RedirectToAudio(IReceiverListenerRegister iReceiverListenerRegister, int bufferSize) {
        this.iReceiverListenerRegister = iReceiverListenerRegister;
        this.bufferSize = bufferSize;
    }

    @Override
    public void redirectOff() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "redirectOff");
        isRedirecting = false;
        iReceiverListenerRegister.registerReceiverListener(null);

        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }

            audioTrack.release();
            audioTrack = null;
        }

        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "redirectOff done");
    }

    public IRedirectCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "start");
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, String.format("start audioInBuffersize is %d", bufferSize));
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
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "run");
        isRedirecting = true;
        audioTrack.play();
        iReceiverListenerRegister.registerReceiverListener(this);
    }

    @Override
    public void onReceiveMessage(byte[] data) {
        if (Settings.debug) Log.i(Tags.NET_REDIR_AUDIO, "onReceiveMessage");
        if (isRedirecting) {
            audioTrack.write(data, 0, bufferSize);
        }
    }
}
