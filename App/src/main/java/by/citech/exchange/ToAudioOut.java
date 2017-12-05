package by.citech.exchange;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class ToAudioOut
        implements IReceiverCtrl, IReceiver {

    private static final String TAG = Tags.TO_AUDOUT;
    private static final boolean debug = Settings.debug;

    //--------------------- settings

    private int audioStreamType;
    private int audioUsage;
    private int audioContentType;
    private int audioEncoding;
    private int audioRate;
    private int audioOutChannel;
    private int audioMode;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;

    {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        audioStreamType = Settings.audioStreamType;
        audioUsage = Settings.audioUsage;
        audioContentType = Settings.audioContentType;
        audioEncoding = Settings.audioEncoding;
        audioRate = Settings.audioRate;
        audioOutChannel = Settings.audioOutChannel;
        audioMode = Settings.audioMode;
        audioBuffSizeBytes = Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
    }

    private void applySettings() {
    }

    //--------------------- non-settings

    private IReceiverReg iReceiverReg;
    private AudioTrack audioTrack;
    private boolean isRedirecting;

    public ToAudioOut(IReceiverReg iReceiverReg) {
        this.iReceiverReg = iReceiverReg;
    }

    @Override
    public void prepareRedirect() {
        if (debug) Log.i(TAG, "prepareRedirect");
        redirectOff();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } else {
            audioTrack = new AudioTrack(
                    audioStreamType,
                    audioRate,
                    audioOutChannel,
                    audioEncoding,
                    audioBuffSizeBytes,
                    audioMode
            );
        }
        if (debug) Log.i(TAG, "prepareRedirect done");
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
        if (isRedirecting || (audioTrack == null)) {
            Log.e(TAG, "redirectOn already redirecting or audioTrack is null");
            return;
        }
        isRedirecting = true;
        audioTrack.play();
        iReceiverReg.registerReceiver(this);
        if (debug) Log.i(TAG, "redirectOn done");
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
