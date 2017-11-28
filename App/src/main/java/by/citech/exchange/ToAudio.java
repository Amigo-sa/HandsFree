package by.citech.exchange;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class ToAudio
        implements IReceiverCtrl, IReceiver {

    private static final String TAG = Tags.TO_AUDIO;
    private static final boolean debug = Settings.debug;

    private int bufferSize = Settings.bufferSize;
    private IReceiverReg iReceiverReg;
    private AudioTrack audioTrack;
    private boolean isRedirecting = false;

    public ToAudio(IReceiverReg iReceiverReg) {
        this.iReceiverReg = iReceiverReg;
    }

    public void prepare() {
        if (debug) Log.i(TAG, "prepare");
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

        if (debug) Log.i(TAG, "prepare done");
    }

    public void run() {
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
            audioTrack.write(data, 0, bufferSize);
        }
    }

    @Override
    public void onReceiveData(short[] data) {
        if (debug) Log.i(TAG, "onReceiveData short[]");
        if (isRedirecting) {
            audioTrack.write(data, 0, bufferSize);
        }
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

}
