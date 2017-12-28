package by.citech.handsfree.debug;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegister;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.codec.audio.AudioCodecType;
import by.citech.handsfree.exchange.IReceiver;
import by.citech.handsfree.exchange.IReceiverCtrl;
import by.citech.handsfree.exchange.IReceiverReg;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ToAudioOut;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class Bt2AudOutLooper
        implements IBase, ITransmitter, IReceiverReg, IPrepareObject,
        ISettingsCtrl, ICallerFsm, ICallerFsmListener, ICallerFsmRegister {

    private static final String STAG = Tags.BT2AUDOUT_LOOPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private AudioCodecType codecType;
    private ICodec codec;
    private IReceiver iReceiver;
    private IReceiverCtrl iReceiverCtrl;
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
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        codec = AudioCodec.getAudioCodec(codecType);
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
        iReceiverCtrl = new ToAudioOut(this);
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
        stopDebug();
        codecType = null;
        codec = null;
        iReceiverCtrl = null;
        IBase.super.baseStop();
        return true;
    }

    //--------------------- ICallerFsmListener

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
            iReceiverCtrl.prepareRedirect();
            iReceiverCtrl.redirectOn();
            codec.initiateEncoder();
            codec.initiateDecoder();
        }
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iReceiver = null;
        iReceiverCtrl.redirectOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void registerReceiver(IReceiver iReceiver) {
        if (debug) Log.i(TAG, "registerReceiver");
        this.iReceiver = iReceiver;
    }

    @Override
    public void sendData(byte[] data) {
        if (data == null || data.length != codecType.getEncodedBytesSize()) {
            if (debug) Log.w(TAG, "sendData byte[]" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        short[] dataDecoded = codec.getDecodedData(data);
//        if (debug) Log.w(TAG, String.format(Locale.US,
//                "sendData byte[] data received length is %d, toString is %s",
//                data.length,
//                Arrays.toString(data)));
//        if (debug) Log.w(TAG, String.format(Locale.US,
//                "sendData byte[] data decoded length is %d, toString is %s",
//                dataDecoded.length,
//                Arrays.toString(dataDecoded)));
        if (iReceiver != null) {
            if (!isSession) {
                if (debug) Log.i(TAG, "sendData byte[], first sendData on session");
                isSession = true;
            }
            iReceiver.onReceiveData(dataDecoded);
        }
    }

}
