package by.citech.exchange;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class ToAudioOut
        implements IReceiverCtrl, IReceiver {

    private final String TAG;
    private final boolean debug;

    private final int audioUsage;
    private final int audioContentType;
    private final int audioEncoding;
    private final int audioRate;
    private final int audioOutChannel;
    private final int audioMode;
    private final int audioBuffSizeBytes;
    private final int audioBuffSizeShorts;

    {
        TAG = Tags.TO_AUDOUT;
        debug = Settings.debug;

        audioUsage = Settings.audioUsage;
        audioContentType = Settings.audioContentType;
        audioEncoding = Settings.audioEncoding;
        audioRate = Settings.audioRate;
        audioOutChannel = Settings.audioOutChannel;
        audioMode = Settings.audioMode;
        audioBuffSizeBytes = Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
    }

    private IReceiverReg iReceiverReg;
    private AudioTrack audioTrack;
    private boolean isRedirecting;

    public ToAudioOut(IReceiverReg iReceiverReg) {
        this.iReceiverReg = iReceiverReg;
    }

    @Override
    public void prepareRedirect() {
        if (debug) Log.i(TAG, "prepareStream");
        redirectOff();
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(audioUsage)
                        .setContentType(audioContentType)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioEncoding)
                        .setSampleRate(audioRate)
                        .setChannelMask(audioOutChannel)
                        .build())
                .setBufferSizeInBytes(audioBuffSizeBytes)
                .setTransferMode(audioMode)
                .build();
        if (debug) Log.i(TAG, "prepareStream done");
    }

    @Override
    public void redirectOff() {
        if (debug) Log.i(TAG, "redirectOff");
        isRedirecting = false;
        iReceiverReg.registerReceiver(null);
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }
            audioTrack.release();
            audioTrack = null;
        }
        if (debug) Log.i(TAG, "redirectOff done");
    }

    @Override
    public void redirectOn() {
        if (debug) Log.i(TAG, "run");
        isRedirecting = true;
        audioTrack.play();
        iReceiverReg.registerReceiver(this);
        if (debug) Log.i(TAG, "run done");
    }

    @Override
    public void onReceiveData(byte[] data) {
        if (debug) Log.i(TAG, "onReceiveData byte[]");
        if (isRedirecting) {
            audioTrack.write(data, 0, audioBuffSizeBytes);
        }
    }

    @Override
    public void onReceiveData(short[] data) {
        if (debug) Log.i(TAG, "onReceiveData short[]");
        if (isRedirecting) {
            audioTrack.write(data, 0, audioBuffSizeShorts);
        }
    }

}
