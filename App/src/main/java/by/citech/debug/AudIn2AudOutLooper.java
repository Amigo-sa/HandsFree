package by.citech.debug;

import android.util.Log;

import java.util.Arrays;

import by.citech.codec.audio.AudioCodec;
import by.citech.codec.audio.AudioCodecType;
import by.citech.exchange.FromAudioIn;
import by.citech.exchange.IReceiver;
import by.citech.exchange.IReceiverCtrl;
import by.citech.exchange.IReceiverReg;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.exchange.ToAudioOut;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class AudIn2AudOutLooper
        implements IDebugListener, IReceiverReg, ITransmitter, IDebugCtrl {

    private String TAG;
    private boolean debug;

    private AudioCodecType codecType;
    private int codecFactor;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private int buff2CodecFactor;
    private boolean audioSinglePacket;

    {
        TAG = Tags.AUDIN2AUDOUT_LOOPER;
        debug = Settings.debug;

        codecType = Settings.codecType;
        codecFactor = codecType.getDecodedShortsSize();
        audioBuffSizeBytes = Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
        buff2CodecFactor = audioBuffSizeShorts / codecFactor;
        audioSinglePacket = Settings.audioSinglePacket;
    }

    private AudioCodec audioCodec;
    private IReceiver iReceiver;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiverCtrl iReceiverCtrl;
    private short[] dataBuff;

    public AudIn2AudOutLooper() {
        iReceiverCtrl = new ToAudioOut(this);
        iTransmitterCtrl = new FromAudioIn(this);
        audioCodec = new AudioCodec(codecType);
        dataBuff = new short[audioBuffSizeShorts];
    }

    @Override
    public void activate() {
        if (debug) Log.i(TAG, "run");
        audioCodec.initiateEncoder();
        audioCodec.initiateDecoder();
        iReceiverCtrl.prepareRedirect();
        iTransmitterCtrl.prepareStream();
        iReceiverCtrl.redirectOn();
        new Thread(() -> iTransmitterCtrl.streamOn()).start();
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
        if (iReceiver != null) {
            if (audioSinglePacket) {
//              iReceiver.onReceiveData(data);
                iReceiver.onReceiveData(audioCodec.getDecodedData(audioCodec.getEncodedData(data)));
            } else {
                dataBuff = data;
                int from;
                for (int i = 0; i < buff2CodecFactor; i++) {
                    from = i * codecFactor;
                    if (debug) Log.i(TAG, "sendData from is " + from);
                    System.arraycopy(audioCodec.getDecodedData(audioCodec.getEncodedData(Arrays.copyOfRange(dataBuff, from, from + codecFactor))), 0, dataBuff, from, codecFactor);
                }
                iReceiver.onReceiveData(dataBuff);
            }
        }
    }

}
