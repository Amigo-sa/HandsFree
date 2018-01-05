package by.citech.handsfree.debug;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegister;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.codec.audio.AudioCodecType;
import by.citech.handsfree.exchange.FromAudioIn;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.ToAudioOut;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.threading.IThreadManager;

public class AudIn2AudOutLooper
        implements ITransmitter, IBase, IPrepareObject, IThreadManager,
        ISettingsCtrl, ICallerFsmRegister, ICallerFsmListener, ICallerFsm {

    private static final String STAG = Tags.AUDIN2AUDOUT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private AudioCodecType codecType;
    private ICodec codec;
    private int codecFactor;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private int buff2CodecFactor;
    private boolean audioSingleFrame;
    private ITransmitter iTransmitter;
    private ITransmitterCtrl fromCtrl, toCtrl;
    private boolean isUsingCodec;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        applySettings(null);
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return codecType != null && codec != null;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        codecType = Settings.audioCodecType;
        codecFactor = codecType.getDecodedShortsSize();
        audioBuffSizeBytes = Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
        buff2CodecFactor = audioBuffSizeShorts / codecFactor;
        audioSingleFrame = Settings.audioSingleFrame;
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        codec = AudioCodec.getAudioCodec(codecType);
        return true;
    }

    //--------------------- constructor

    public AudIn2AudOutLooper(boolean isUsingCodec) {
        this.isUsingCodec = isUsingCodec;
        ToAudioOut toAudioOut = new ToAudioOut();
        iTransmitter = toAudioOut;
        toCtrl = toAudioOut;
        fromCtrl = new FromAudioIn();
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        try {
            toCtrl.prepareStream(null);
            fromCtrl.prepareStream(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        unregisterCallerFsmListener(this, TAG);
        stopDebug();
        fromCtrl.finishStream();
        toCtrl.finishStream();
        iTransmitter = null;
        fromCtrl = null;
        toCtrl = null;
        codecType = null;
        codec = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- ICallerFsmListener

    @Override
    public void onCallerStateChange(CallerState from, CallerState to, ECallReport why) {
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
        if (iTransmitter != null) {
            if (debug) Log.i(TAG, "sendData data sended");
            iTransmitter.sendData(data);
        }
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        if (iTransmitter != null) {
            if (audioSingleFrame) {
                iTransmitter.sendData(getPreparedData(data));
            } else {
                int from;
                for (int i = 0; i < buff2CodecFactor; i++) {
                    from = i * codecFactor;
                    if (debug) Log.i(TAG, "sendData from is " + from);
                    System.arraycopy(getPreparedData(Arrays.copyOfRange(data, from, from + codecFactor)), 0, data, from, codecFactor);
                }
                iTransmitter.sendData(data);
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
