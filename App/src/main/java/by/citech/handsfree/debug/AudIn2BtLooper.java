package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.settings.enumeration.AudioCodecType;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.FromAudioIn;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.ToBluetooth;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class AudIn2BtLooper
        implements IDebugCtrl, IBase, ITransmitter, IReceiverReg, IPrepareObject, ISettingsCtrl {

    private static final String STAG = Tags.AUDIN2BT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private AudioCodecType codecType;
    private ICodec codec;
    private IReceiverCtrl iReceiverCtrl;
    private ITransmitterCtrl iTransmitterCtrl;
    private IReceiver iReceiver;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        takeSettings();
        applySettings(null);
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return codec != null && codecType != null;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        codecType = Settings.audioCodecType;
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        codec = AudioCodec.getAudioCodec(codecType);
        return true;
    }

    //--------------------- constructor

    public AudIn2BtLooper(StorageData<byte[][]> micToBtStorage) {
        iTransmitterCtrl = new FromAudioIn(this);
        iReceiverCtrl = new ToBluetooth(this, micToBtStorage);
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        prepareObject();
        return false;
    }

    @Override
    public boolean baseStop() {
        IBase.super.baseStop();
        if (debug) Log.i(TAG, "baseStop");
        stopDebug();
        iReceiverCtrl = null;
        iTransmitterCtrl = null;
        return true;
    }

    //--------------------- IDebugCtrl

    @Override
    public void startDebug() {
        if (debug) Log.i(TAG, "startDebug");
        if (iReceiver == null) {
            codec.initiateEncoder();
            codec.initiateDecoder();
            iReceiverCtrl.prepareRedirect();
            iReceiverCtrl.redirectOn();
            iTransmitterCtrl.prepareStream();
            new Thread(() -> iTransmitterCtrl.streamOn()).start();
        }
    }

    @Override
    public void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iReceiver = null;
        iReceiverCtrl.redirectOff();
        iTransmitterCtrl.streamOff();
    }

    //--------------------- main

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        if (iReceiver != null) {
            iReceiver.onReceiveData(codec.getEncodedData(data));
        }
    }

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        this.iReceiver = iReceiver;
    }

}
