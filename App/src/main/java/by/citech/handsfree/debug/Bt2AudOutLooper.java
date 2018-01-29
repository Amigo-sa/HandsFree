package by.citech.handsfree.debug;

import android.util.Log;

import by.citech.handsfree.call.fsm.CallFsm;
import by.citech.handsfree.codec.audio.AudioCodecFactory;
import by.citech.handsfree.codec.audio.ICodec;
import by.citech.handsfree.common.IBuilding;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.call.fsm.ECallState;
import by.citech.handsfree.call.fsm.ECallReport;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.exchange.consumers.ToAudioOut;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class Bt2AudOutLooper
        implements IRxComplex, CallFsm.ICallFsmReporter, CallFsm.ICallFsmListener, CallFsm.ICallFsmListenerRegister, IBuilding {

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
        if (debug) Log.i(TAG, "build");
        registerCallFsmListener(this, TAG);
        try {
            iStreamer.prepareStream(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        if (debug) Log.i(TAG, "destroy");
        unregisterCallFsmListener(this, TAG);
        stopDebug();
        iStreamer.finishStream();
        iStreamer = null;
        iRxComplex = null;
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
        iStreamer.streamOn();
        codec.initiateEncoder();
        codec.initiateDecoder();
    }

    private void stopDebug() {
        if (debug) Log.i(TAG, "stopDebug");
        iStreamer.streamOff();
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
        iRxComplex.sendData(dataDecoded);
    }

}
