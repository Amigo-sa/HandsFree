package by.citech.handsfree.loopers;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.logic.ECallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegisterListener;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.ESeverityLevel;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.management.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class Bt2AudOutLooper
        implements IBase, ITransmitter, IPrepareObject,
        ISettingsCtrl, ICallerFsm, ICallerFsmListener, ICallerFsmRegisterListener {

    private static final String STAG = Tags.Bt2AudOutLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private EAudioCodecType codecType;
    private ICodec codec;
    private ITransmitter iTransmitter;
    private ITransmitterCtrl iTransmitterCtrl;
    private boolean isSession;

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
        return codec != null && codecType != null;
    }

    @Override
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        codec = AudioCodecFactory.getAudioCodec(codecType);
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        codecType = Settings.audioCodecType;
        return true;
    }

    //--------------------- constructor

    public Bt2AudOutLooper() {
        ToAudioOut toAudioOut = new ToAudioOut();
        iTransmitterCtrl = toAudioOut;
        iTransmitter = toAudioOut;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        try {
            iTransmitterCtrl.prepareStream(this);
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
        iTransmitterCtrl.finishStream();
        iTransmitterCtrl = null;
        iTransmitter = null;
        codecType = null;
        codec = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- ICallerFsmListener

    public void onCallerStateChange(ECallerState from, ECallerState to, ECallReport why) {
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
        iTransmitterCtrl.streamOn();
        codec.initiateEncoder();
        codec.initiateDecoder();
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iTransmitterCtrl.streamOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void sendData(byte[] data) {
        if (data == null || data.length != codecType.getEncodedBytesSize()) {
            if (debug) Log.w(TAG, "sendData byte[]" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        short[] dataDecoded = codec.getDecodedData(data);
//      if (debug) Log.w(TAG, String.format(Locale.US,
//              "sendData byte[] data received length is %d, toString is %s",
//              data.length,
//              Arrays.toString(data)));
//      if (debug) Log.w(TAG, String.format(Locale.US,
//              "sendData byte[] data decoded length is %d, toString is %s",
//              dataDecoded.length,
//              Arrays.toString(dataDecoded)));
        if (!isSession) {
            if (debug) Log.i(TAG, "sendData byte[], first sendData on session");
            isSession = true;
        }
        iTransmitter.sendData(dataDecoded);
    }

}
