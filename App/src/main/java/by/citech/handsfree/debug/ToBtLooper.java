package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodec;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.exchange.FromDataGenerator;
import by.citech.handsfree.logic.CallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegister;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.codec.audio.AudioCodecType;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.FromAudioIn;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.ToBluetooth;
import by.citech.handsfree.common.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.enumeration.DataSource;
import by.citech.handsfree.threading.IThreadManager;

public class ToBtLooper
        implements IBase, ITransmitter, IPrepareObject, IThreadManager,
        ISettingsCtrl, ICallerFsm, ICallerFsmListener, ICallerFsmRegister {

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
    private ITransmitterCtrl fromCtrl, toCtrl;
    private ITransmitter iTransmitter;
    private boolean isSession;

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
        ISettingsCtrl.super.applySettings(severityLevel);
        codec = AudioCodec.getAudioCodec(codecType);
        return true;
    }

    //--------------------- constructor

    public ToBtLooper(StorageData<byte[][]> micToBtStorage, DataSource dataSource) throws Exception {
        if (dataSource == null || micToBtStorage == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        switch (dataSource) {
            case MICROPHONE:
                fromCtrl = new FromAudioIn();
                break;
            case DATAGENERATOR:
                fromCtrl = new FromDataGenerator(codecType.getDecodedShortsSize(), 10, true);
                break;
        }
        ToBluetooth toBluetooth = new ToBluetooth(micToBtStorage);
        iTransmitter = toBluetooth;
        toCtrl = toBluetooth;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        try {
            fromCtrl.prepareStream(this);
            toCtrl.prepareStream(this);
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
        toCtrl.finishStream();
        fromCtrl.finishStream();
        toCtrl = null;
        fromCtrl = null;
        codecType = null;
        codec = null;
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
        codec.initiateEncoder();
        codec.initiateDecoder();
        toCtrl.streamOn();
        addRunnable(() -> fromCtrl.streamOn());
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        toCtrl.streamOff();
        fromCtrl.streamOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void sendData(short[] data) {
        if (data == null || data.length != codecType.getDecodedShortsSize()) {
            if (debug) Log.w(TAG, "sendData short[]" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (iTransmitter != null) {
            if (!isSession) {
                if (debug) Log.i(TAG, "sendData short[], first sendData on session");
                isSession = true;
            }
            iTransmitter.sendData(codec.getEncodedData(data));
        }
    }

}
