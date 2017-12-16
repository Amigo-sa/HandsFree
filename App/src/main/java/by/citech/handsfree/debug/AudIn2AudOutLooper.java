package by.citech.handsfree.debug;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.enumeration.AudioCodecType;
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

public class AudIn2AudOutLooper
        implements IDebugCtrl, IReceiverReg, ITransmitter, IBase, IPrepareObject, ISettingsCtrl {

    private static final String TAG = Tags.AUDIN2AUDOUT_LOOPER;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private AudioCodecType codecType;
    private AudioCodec audioCodec;
    private int codecFactor;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private int buff2CodecFactor;
    private boolean audioSingleFrame;
    private short[] dataBuff;
    private boolean isPrepared;

    {
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        applySettings();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        isPrepared = true;
        return isPrepared;
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

    public boolean applySettings() {
        dataBuff = new short[audioBuffSizeShorts];
        audioCodec = new AudioCodec(codecType);
        return true;
    }

    //--------------------- non-settings

    private IReceiver iReceiver;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiverCtrl iReceiverCtrl;

    public AudIn2AudOutLooper() {
        iReceiverCtrl = new ToAudioOut(this);
        iTransmitterCtrl = new FromAudioIn(this);
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        prepareObject();
        if (debug) Log.i(TAG, "baseStart");
        return true;
    }

    @Override
    public boolean baseStop() {
//        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        iTransmitterCtrl = null;
        iReceiverCtrl = null;
        codecType = null;
        audioCodec = null;
        dataBuff = null;
        return true;
    }

    //--------------------- IDebugCtrl

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        if (iReceiver == null) {
            audioCodec.initiateEncoder();
            audioCodec.initiateDecoder();
            iReceiverCtrl.prepareRedirect();
            iTransmitterCtrl.prepareStream();
            iReceiverCtrl.redirectOn();
            new Thread(() -> iTransmitterCtrl.streamOn()).start();
        }
    }

    @Override
    public void stopDebug() {
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
