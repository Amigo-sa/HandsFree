package by.citech.codec.audio;

import android.util.Log;

public class AudioCodec implements ICodec {

    private static final String TAG = "WSD_AudioCodec";
    private static final boolean debug = true;

    private ICodec codec;

    public AudioCodec(AudioCodecType audioCodecType) {
        switch (audioCodecType) {
            case Sit_3_0_java:
                Log.i(TAG, "AudioCodec audioCodecType default Sit_3_0_java");
                codec = new SitAudioCodec_3_0_java();
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
