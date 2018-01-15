package by.citech.handsfree.loopers;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.exchange.producers.FromGenerator;
import by.citech.handsfree.generator.EDataType;
import by.citech.handsfree.logic.ECallerState;
import by.citech.handsfree.logic.ECallReport;
import by.citech.handsfree.logic.ICallerFsm;
import by.citech.handsfree.logic.ICallerFsmListener;
import by.citech.handsfree.logic.ICallerFsmRegisterListener;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.ESeverityLevel;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.exchange.consumers.ToBluetooth;
import by.citech.handsfree.management.IBase;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreadManager;

public class ToBtLooper
        implements IBase, ITransmitter, IPrepareObject, IThreadManager,
        ISettingsCtrl, ICallerFsm, ICallerFsmListener, ICallerFsmRegisterListener {

    private static final String STAG = Tags.ToBtLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private EAudioCodecType codecType;
    private ICodec codec;
    private ITransmitterCtrl source, destination;
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
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        codec = AudioCodecFactory.getAudioCodec(codecType);
        return true;
    }

    //--------------------- constructor

    public ToBtLooper(StorageData<byte[][]> micToBtStorage, EDataSource dataSource) throws Exception {
        if (dataSource == null || micToBtStorage == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        switch (dataSource) {
            case MICROPHONE:
                source = new FromAudioIn();
                break;
            case DATAGENERATOR:
                source = new FromGenerator(codecType.getDecodedShortsSize(), 8, true, EDataType.Sine);
                break;
        }
        ToBluetooth toBluetooth = new ToBluetooth(micToBtStorage);
        iTransmitter = toBluetooth;
        destination = toBluetooth;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        registerCallerFsmListener(this, TAG);
        try {
            source.prepareStream(this);
            destination.prepareStream(null);
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
        destination.finishStream();
        source.finishStream();
        destination = null;
        source = null;
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
        codec.initiateEncoder();
        codec.initiateDecoder();
        destination.streamOn();
        addRunnable(() -> source.streamOn());
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        destination.streamOff();
        source.streamOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void sendData(short[] data) {
        if (debug) Log.w(TAG, "sendData short[] row (shorts): " + Arrays.toString(data));
        if (data == null || data.length != codecType.getDecodedShortsSize()) {
            if (debug) Log.w(TAG, "sendData short[]" + StatusMessages.ERR_PARAMETERS);
        } else if (iTransmitter != null) {
            if (!isSession) {
                if (debug) Log.i(TAG, "sendData short[], first sendData on session");
                isSession = true;
            }
            byte[] toSend = codec.getEncodedData(data);
            if (debug) Log.w(TAG, "sendData short[] encoded (bytes): " + Arrays.toString(toSend));
            iTransmitter.sendData(toSend);
//          iTransmitter.sendData(codec.getEncodedData(data));
        }
    }

}
