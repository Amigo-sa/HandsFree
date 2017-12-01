package by.citech.debug;

import android.util.Log;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.data.StorageData;
import by.citech.exchange.FromMic;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.exchange.ToBluetooth;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class DebugMicToBtLooper
        implements IDebugListener, IDebugCtrl, ITransmitter, IReceiverReg {

    private static final String TAG = Tags.AUD2BT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static final AudioCodecType codecType = Settings.codecType;

    private AudioCodec audioCodec;
    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private boolean isRunning;
    private IReceiver iReceiver;

    public DebugMicToBtLooper(StorageData<byte[][]> micToBtStorage) {
        iTransmitterCtrl = new FromMic(this);
        iReceiverCtrl = new ToBluetooth(this, micToBtStorage);
        audioCodec = new AudioCodec(codecType);
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
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
