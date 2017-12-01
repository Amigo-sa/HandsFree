package by.citech.debug;

import android.util.Log;

import java.util.Arrays;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.exchange.FromMic;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.exchange.ToAudio;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class DebugMicToAudLooper
        implements IDebugListener, IReceiverReg, ITransmitter, IDebugCtrl {

    private static final String TAG = Tags.MIC2AUD_LOOPER;
    private static final boolean debug = Settings.debug;
    private static final AudioCodecType codecType = Settings.codecType;
    private static final int codecFactor = codecType.getDecodedShortCnt();
    private static final int buffersize = Settings.audioBufferSize;
    private static final int bufferToCodecFactor = buffersize / codecFactor;

    private AudioCodec audioCodec;
    private IReceiver iReceiver;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiverCtrl iReceiverCtrl;
    private short[] dataBuffer;

    public DebugMicToAudLooper() {
        iReceiverCtrl = new ToAudio(this);
        iTransmitterCtrl = new FromMic(this);
        audioCodec = new AudioCodec(codecType);
        dataBuffer = new short[buffersize];
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
        audioCodec.initiateEncoder();
        audioCodec.initiateDecoder();
        iReceiverCtrl.prepareRedirect();
        iTransmitterCtrl.prepareStream();
        iReceiverCtrl.redirectOn();
        iTransmitterCtrl.streamOn();
    }

    @Override
    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        iTransmitterCtrl.streamOff();
        iReceiverCtrl.redirectOff();
    }

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
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
            if (debug) Log.i(TAG, "sendData data sended");
            iReceiver.onReceiveData(data);
        }
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        dataBuffer = data;
        int from;
        if (iReceiver != null) {
            for (int i = 0; i < bufferToCodecFactor; i++) {
                from = i*codecFactor;
                if (debug) Log.i(TAG, "sendData from is " + from);
                System.arraycopy(audioCodec.getDecodedData(audioCodec.getEncodedData(Arrays.copyOfRange(dataBuffer, from, from + codecFactor))), 0, dataBuffer, from, codecFactor);
            }
            iReceiver.onReceiveData(dataBuffer);
//          iReceiver.onReceiveData(data);
//          iReceiver.onReceiveData(audioCodec.getDecodedData(audioCodec.getEncodedData(data)));
        }
    }

}
