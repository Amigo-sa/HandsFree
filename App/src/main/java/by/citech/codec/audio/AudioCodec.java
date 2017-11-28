package by.citech.codec.audio;

import android.util.Log;

public class AudioCodec implements ICodec {

    private static final String TAG = "WSD_AudioCodec";
    private static final boolean debug = true;

    private AudioCodecType audioCodecType;
    private ICodec codec;

    public AudioCodec(AudioCodecType audioCodecType) {
        this.audioCodecType = audioCodecType;
        switch (this.audioCodecType) {
            case Sit:
            default:
                Log.i(TAG, "AudioCodec audioCodecType default Sit");
                codec = new TrashSitAudioCodec();
                this.audioCodecType = AudioCodecType.Sit;
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
