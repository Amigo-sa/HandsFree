package by.citech.handsfree.debug;

import java.util.Arrays;

import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.debug.fsm.EDebugReport;
import by.citech.handsfree.debug.fsm.EDebugState;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.exchange.consumers.ToBluetooth;
import by.citech.handsfree.exchange.producers.FromAudioIn;
import by.citech.handsfree.exchange.producers.FromGenerator;
import by.citech.handsfree.generator.EDataType;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.EDataSource;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.threading.IThreading;
import timber.log.Timber;

public class ToBtLooper
        implements IRxComplex, IThreading, IBuilding,
        DebugFsm.IDebugFsmListenerRegister,
        DebugFsm.IDebugFsmListener,
        DebugFsm.IDebugFsmReporter {

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
        if (debug) Timber.tag(TAG).i("build");
        registerDebugFsmListener(this, TAG);
        try {
            source.prepareStream(this);
            destination.prepareStream(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        if (debug) Timber.tag(TAG).i("destroy");
        unregisterDebugFsmListener(this, TAG);
        stopDebug();
        destination.finishStream();
        source.finishStream();
        destination = null;
        source = null;
        codecType = null;
        codec = null;
    }

    //--------------------- ICallFsmListener

    @Override
    public void onFsmStateChange(EDebugState from, EDebugState to, EDebugReport why) {
        if (debug) Timber.tag(TAG).i("onFsmStateChange");
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
        if (debug) Timber.tag(TAG).i("startDebug");
        codec.initiateEncoder();
        codec.initiateDecoder();
        destination.streamOn();
        addRunnable(() -> source.streamOn());
    }

    private void stopDebug() {
        if (debug) Timber.tag(TAG).i("stopDebug");
        destination.streamOff();
        source.streamOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void sendData(short[] data) {
        if (debug) Timber.tag(TAG).w("sendData short[] row (shorts): %s", Arrays.toString(data));
        if (data == null || data.length != codecType.getDecodedShortsSize()) {
            if (debug) Timber.tag(TAG).w("sendData short[]%s", StatusMessages.ERR_PARAMETERS);
        } else if (iRxComplex != null) {
            if (!isSession) {
                if (debug) Timber.tag(TAG).i("sendData short[], first sendData on session");
                isSession = true;
            }
            byte[] toSend = codec.getEncodedData(data);
            if (debug) Timber.tag(TAG).w("sendData short[] encoded (bytes): %s", Arrays.toString(toSend));
            iRxComplex.sendData(toSend);
//          iRxComplex.sendData(codec.getEncodedData(data));
        }
    }

}
