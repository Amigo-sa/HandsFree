package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.settings.enumeration.AudioCodecType;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ToAudioOut;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class Bt2AudOutLooper
        implements IDebugCtrl, IBase, ITransmitter, IReceiverReg, IPrepareObject, ISettingsCtrl {

    private static final String TAG = Tags.BT2AUDOUT_LOOPER;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private AudioCodecType codecType;
    private AudioCodec audioCodec;

    {
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
        return audioCodec != null;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        audioCodec = new AudioCodec(codecType);
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        codecType = Settings.audioCodecType;
        return true;
    }

    //--------------------- non-settings

    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;

    public Bt2AudOutLooper() {
        iReceiverCtrl = new ToAudioOut(this);
    }

    @Override
    public boolean baseStart() {
        if (debug) Log.i(TAG, "baseStart");
        IBase.super.baseStart();

        return true;
    }

    @Override
    public boolean baseStop() {
        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        codecType = null;
        audioCodec = null;
        iReceiverCtrl = null;
        return true;
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
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData byte[]");
        if (iReceiver != null) {
            iReceiver.onReceiveData(audioCodec.getDecodedData(data));
        }
    }

}
