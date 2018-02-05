package by.citech.handsfree.debug;

import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.codec.ICodec;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.debug.fsm.DebugFsm;
import by.citech.handsfree.debug.fsm.EDebugReport;
import by.citech.handsfree.debug.fsm.EDebugState;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class Bt2AudOutLooper
        implements IRxComplex, IBuilding,
        DebugFsm.IDebugFsmListenerRegister,
        DebugFsm.IDebugFsmListener,
        DebugFsm.IDebugFsmReporter {

    private static final String STAG = Tags.Bt2AudOutLooper;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private EAudioCodecType codecType;
    private ICodec codec;
    private IRxComplex iRxComplex;
    private IStreamer iStreamer;
    private boolean isSession;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        codecType = Settings.AudioCommon.audioCodecType;
        codec = AudioCodecFactory.getAudioCodec(codecType);
    }

    //--------------------- constructor

    public Bt2AudOutLooper() {
        ToAudioOut toAudioOut = new ToAudioOut();
        iStreamer = toAudioOut;
        iRxComplex = toAudioOut;
    }

    //--------------------- IBuilding

    @Override
    public void build() {
        Timber.tag(TAG).i("build");
        registerDebugFsmListener(this, TAG);
        try {
            iStreamer.prepareStream(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        Timber.tag(TAG).i("destroy");
        unregisterDebugFsmListener(this, TAG);
        stopDebug();
        iStreamer.finishStream();
        iStreamer = null;
        iRxComplex = null;
        codecType = null;
        codec = null;
    }

    //--------------------- ICallFsmListener

    public void onFsmStateChange(EDebugState from, EDebugState to, EDebugReport why) {
        Timber.tag(TAG).i("onFsmStateChange");
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
        Timber.tag(TAG).i("startDebug");
        iStreamer.streamOn();
        codec.initiateEncoder();
        codec.initiateDecoder();
    }

    private void stopDebug() {
        Timber.tag(TAG).i("stopDebug");
        iStreamer.streamOff();
        isSession = false;
    }

    //--------------------- main

    @Override
    public void sendData(byte[] data) {
        if (data == null || data.length != codecType.getEncodedBytesSize()) {
            Timber.w("sendData byte[]%s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        short[] dataDecoded = codec.getDecodedData(data);
//      Timber.w(String.format(Locale.US,
//              "sendData byte[] data received length is %d, toString is %s",
//              data.length,
//              Arrays.toString(data)));
//      Timber.w(String.format(Locale.US,
//              "sendData byte[] data decoded length is %d, toString is %s",
//              dataDecoded.length,
//              Arrays.toString(dataDecoded)));
        if (!isSession) {
            Timber.tag(TAG).i("sendData byte[], first sendData on session");
            isSession = true;
        }
        iRxComplex.sendData(dataDecoded);
    }

}
