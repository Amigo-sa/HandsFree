package by.citech.handsfree.debug;

import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.exchange.producers.FromGenerator;
import by.citech.handsfree.generator.EDataType;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.consumers.ToBluetooth;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.threading.IThreading;

public class ToBtLooper
        implements IRxComplex, IThreading, IBuilding,
        CallFsm.ICallFsmReporter, CallFsm.ICallFsmListener, CallFsm.ICallFsmListenerRegister {

    private static final String STAG = Tags.ToBtLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private EAudioCodecType codecType;
    private ICodec codec;
    private IStreamer source, destination;
    private IRxComplex iRxComplex;
    private boolean isSession;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        codecType = Settings.AudioCommon.audioCodecType;
        codec = AudioCodecFactory.getAudioCodec(codecType);
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
        iRxComplex = toBluetooth;
        destination = toBluetooth;
    }

    //--------------------- IBuilding

    @Override
    public void build() {
        if (debug) Log.i(TAG, "build");
        registerCallFsmListener(this, TAG);
        try {
            source.prepareStream(this);
            destination.prepareStream(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        if (debug) Log.i(TAG, "destroy");
        unregisterCallFsmListener(this, TAG);
        stopDebug();
        destination.finishStream();
        source.finishStream();
        destination = null;
        source = null;
        codecType = null;
        codec = null;
    }

    //--------------------- ICallFsmListener

    public void onCallerStateChange(ECallState from, ECallState to, ECallReport why) {
        if (debug) Log.i(TAG, "onCallerStateChange");
        switch (why) {
            case RP_StartDebug:
                startDebug();
                break;
            case RP_StopDebug:
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
        } else if (iRxComplex != null) {
            if (!isSession) {
                if (debug) Log.i(TAG, "sendData short[], first sendData on session");
                isSession = true;
            }
            byte[] toSend = codec.getEncodedData(data);
            if (debug) Log.w(TAG, "sendData short[] encoded (bytes): " + Arrays.toString(toSend));
            iRxComplex.sendData(toSend);
//          iRxComplex.sendData(codec.getEncodedData(data));
        }
    }

}
