package by.citech.debug;

import android.util.Log;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ToAudioOut;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class Bt2AudOutLooper
        implements IDebugListener, IDebugCtrl, ITransmitter, IReceiverReg {

    private static final String TAG = Tags.BT2AUDOUT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static final AudioCodecType codecType = Settings.codecType;

    private AudioCodec audioCodec;
    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;
    private boolean isRunning;

    public Bt2AudOutLooper() {
        iReceiverCtrl = new ToAudioOut(this);
        audioCodec = new AudioCodec(codecType);
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
        iReceiverCtrl.prepareRedirect();
        iReceiverCtrl.redirectOn();
    }

    @Override
    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isRunning = false;
        iReceiverCtrl.redirectOff();
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        audioCodec.initiateEncoder();
        audioCodec.initiateDecoder();
        isRunning = true;
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        isRunning = false;
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        if (debug) Log.i(TAG, "registerReceiver");
        this.iReceiver = iReceiver;
    }

    @Override
    public void sendMessage(String message) {
        Log.e(TAG, "sendMessage");
    }

    @Override
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData byte[]");
        if (isRunning && (iReceiver != null)) {
            iReceiver.onReceiveData(audioCodec.getDecodedData(data));
        }
    }

    @Override
    public void sendData(short[] data) {
        Log.e(TAG, "sendData short[]");
    }

}
