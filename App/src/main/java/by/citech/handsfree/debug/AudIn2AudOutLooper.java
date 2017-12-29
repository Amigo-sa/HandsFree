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
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.ToAudioOut;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.threading.IThreadManager;

public class AudIn2AudOutLooper
        implements IReceiverReg, ITransmitter, IBase, IPrepareObject, IThreadManager,
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
    private short[] dataBuff;
    private IReceiver iReceiver;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiverCtrl iReceiverCtrl;

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
        return codecType != null && dataBuff != null && codec != null;
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
        dataBuff = new short[audioBuffSizeShorts];
        codec = AudioCodec.getAudioCodec(codecType);
        return true;
    }

    //--------------------- constructor

    public AudIn2AudOutLooper() throws Exception {
        iReceiverCtrl = new ToAudioOut(this);
        iTransmitterCtrl = new FromAudioIn(this);
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        prepareObject();
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        unregisterCallerFsmListener(this, TAG);
        stopDebug();
        iTransmitterCtrl = null;
        iReceiverCtrl = null;
        codecType = null;
        codec = null;
        dataBuff = null;
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
        if (iReceiver == null) {
            codec.initiateEncoder();
            codec.initiateDecoder();
            iReceiverCtrl.prepareRedirect();
            iTransmitterCtrl.prepareStream();
            iReceiverCtrl.redirectOn();
            addRunnable(() -> iTransmitterCtrl.streamOn());
        }
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iReceiver = null;
        iTransmitterCtrl.streamOff();
        iReceiverCtrl.redirectOff();
    }

    //--------------------- main

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        if (debug) Log.i(TAG, "registerReceiver");
        this.iReceiver = iReceiver;
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
            if (audioSingleFrame) {
//              iReceiver.onReceiveData(data);
                iReceiver.onReceiveData(codec.getDecodedData(codec.getEncodedData(data)));
            } else {
                dataBuff = data;
                int from;
                for (int i = 0; i < buff2CodecFactor; i++) {
                    from = i * codecFactor;
                    if (debug) Log.i(TAG, "sendData from is " + from);
                    System.arraycopy(codec.getDecodedData(codec.getEncodedData(Arrays.copyOfRange(dataBuff, from, from + codecFactor))), 0, dataBuff, from, codecFactor);
                }
                iReceiver.onReceiveData(dataBuff);
            }
        }
    }

}
