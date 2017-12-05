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

    //--------------------- settings

    private AudioCodecType codecType;
    private AudioCodec audioCodec;

    {
        takeSettings();
        applySettings();
    }

    private void applySettings() {
        audioCodec = new AudioCodec(codecType);
    }

    private void takeSettings() {
        codecType = Settings.audioCodecType;
    }

    //--------------------- non-settings

    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;

    public Bt2AudOutLooper() {
        iReceiverCtrl = new ToAudioOut(this);
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "activate");
    }

    @Override
    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        stopDebug();
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        if (iReceiver == null) {
            iReceiverCtrl.prepareRedirect();
            iReceiverCtrl.redirectOn();
            audioCodec.initiateEncoder();
            audioCodec.initiateDecoder();
        }
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iReceiver = null;
        iReceiverCtrl.redirectOff();
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
        if (iReceiver != null) {
            iReceiver.onReceiveData(audioCodec.getDecodedData(data));
        }
    }

    @Override
    public void sendData(short[] data) {
        Log.e(TAG, "sendData short[]");
    }

}
