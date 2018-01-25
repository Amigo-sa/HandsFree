package by.citech.handsfree.loopers;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.call.fsm.ICallFsmListener;
import by.citech.handsfree.call.fsm.ICallFsmListenerRegister;
import by.citech.handsfree.call.fsm.ICallFsmReporter;
import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.threading.IThreading;

public class AudIn2AudOutLooper
        implements IRxComplex, IThreading, ICallFsmListenerRegister,
        ICallFsmListener, ICallFsmReporter, IBuilding {

    private static final String STAG = Tags.AudIn2AudOutLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private EAudioCodecType codecType;
    private ICodec codec;
    private int codecFactor;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private int buff2CodecFactor;
    private boolean audioSingleFrame;
    private IRxComplex iRxComplex;
    private IStreamer fromCtrl, toCtrl;
    private boolean isUsingCodec;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        codecType = Settings.AudioCommon.audioCodecType;
        codecFactor = codecType.getDecodedShortsSize();
        audioBuffSizeBytes = Settings.AudioCommon.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
        buff2CodecFactor = audioBuffSizeShorts / codecFactor;
        audioSingleFrame = Settings.AudioCommon.audioSingleFrame;
        codec = AudioCodecFactory.getAudioCodec(codecType);
    }

    //--------------------- constructor

    public AudIn2AudOutLooper(boolean isUsingCodec) {
        this.isUsingCodec = isUsingCodec;
        ToAudioOut toAudioOut = new ToAudioOut();
        iRxComplex = toAudioOut;
        toCtrl = toAudioOut;
        fromCtrl = new FromAudioIn();
    }

    //--------------------- IBuilding

    @Override
    public void build() {
        if (debug) Log.i(TAG, "build");
        registerCallerFsmListener(this, TAG);
        try {
            toCtrl.prepareStream(null);
            fromCtrl.prepareStream(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        if (debug) Log.i(TAG, "destroy");
        unregisterCallerFsmListener(this, TAG);
        stopDebug();
        fromCtrl.finishStream();
        toCtrl.finishStream();
        iRxComplex = null;
        fromCtrl = null;
        toCtrl = null;
        codecType = null;
        codec = null;
    }

    //--------------------- ICallFsmListener

    @Override
    public void onCallerStateChange(ECallState from, ECallState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case StartDebug:
                startDebug();
                break;
            case StopDebug:
                stopDebug();
                break;
            default:
                break;
        }
    }

    private void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        codec.initiateEncoder();
        codec.initiateDecoder();
        toCtrl.streamOn();
        addRunnable(() -> fromCtrl.streamOn());
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        fromCtrl.streamOff();
        toCtrl.streamOff();
    }

    //--------------------- main

    @Override
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData byte[]");
        if (iRxComplex != null) {
            if (debug) Log.i(TAG, "sendData data sended");
            iRxComplex.sendData(data);
        }
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        if (iRxComplex != null) {
            if (audioSingleFrame) {
                iRxComplex.sendData(getPreparedData(data));
            } else {
                int from;
                for (int i = 0; i < buff2CodecFactor; i++) {
                    from = i * codecFactor;
                    if (debug) Log.i(TAG, "sendData from is " + from);
                    System.arraycopy(getPreparedData(Arrays.copyOfRange(data, from, from + codecFactor)), 0, data, from, codecFactor);
                }
                iRxComplex.sendData(data);
            }
        }
    }

    private short[] getPreparedData(short[] data) {
        if (isUsingCodec) {
            return data;
        } else {
            return codec.getDecodedData(codec.getEncodedData(data));
        }
    }

}
