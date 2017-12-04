package by.citech.debug;

import android.util.Log;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.data.StorageData;
import by.citech.exchange.FromAudioIn;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.exchange.ToBluetooth;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class AudIn2BtLooper
        implements IDebugListener, IDebugCtrl, ITransmitter, IReceiverReg {

    private static final String TAG = Tags.AUDINBT_LOOPER;
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
        codecType = Settings.codecType;
    }

    //--------------------- non-settings

    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private boolean isRunning;
    private IReceiver iReceiver;

    public AudIn2BtLooper(StorageData<byte[][]> micToBtStorage) {
        iTransmitterCtrl = new FromAudioIn(this);
        iReceiverCtrl = new ToBluetooth(this, micToBtStorage);
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
        iReceiverCtrl.prepareRedirect();
        iReceiverCtrl.redirectOn();
        iTransmitterCtrl.prepareStream();
        new Thread(() -> iTransmitterCtrl.streamOn()).start();
    }

    @Override
    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isRunning = false;
        iReceiverCtrl.redirectOff();
        iTransmitterCtrl.streamOff();
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
    public void sendMessage(String message) {
        Log.e(TAG, "sendMessage");
    }

    @Override
    public void sendData(byte[] data) {
        Log.e(TAG, "sendData byte[]");
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        if (isRunning && (iReceiver != null)) {
            iReceiver.onReceiveData(audioCodec.getEncodedData(data));
        }
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        this.iReceiver = iReceiver;
    }

}
