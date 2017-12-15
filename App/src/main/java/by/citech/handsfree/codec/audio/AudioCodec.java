package by.citech.handsfree.codec.audio;

import android.util.Log;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.enumeration.AudioCodecType;

public class AudioCodec implements ICodec {

    private static final String TAG = Tags.AUDCODEC;
    private static final boolean debug = Settings.debug;

    private ICodec codec;

    public AudioCodec(AudioCodecType audioCodecType) {
        switch (audioCodecType) {
            case Sit_2_1_java:
            default:
                Log.i(TAG, "AudioCodec audioCodecType default Sit_2_1_java");
                codec = new SitAudioCodec_2_1_java();
                break;
        }
    }

    @Override
    public void initiateDecoder() {
        if (debug) Log.i(TAG, "initiateDecoder");
        codec.initiateDecoder();
    }

    @Override
    public void initiateEncoder() {
        if (debug) Log.i(TAG, "initiateEncoder");
        codec.initiateEncoder();
    }

    public short[] getDecodedData(byte[] dataToDecode) {
        if (debug) Log.i(TAG, "getDecodedData");
        return codec.getDecodedData(dataToDecode);
    }

    public byte[] getEncodedData(short[] dataToEncode) {
        if (debug) Log.i(TAG, "getEncodedData");
        return codec.getEncodedData(dataToEncode);
    }

}
