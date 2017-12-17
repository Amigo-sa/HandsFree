package by.citech.handsfree.codec.audio;

import android.util.Log;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.enumeration.AudioCodecType;

public class AudioCodec {

    private static final String TAG = Tags.AUDCODEC;
    private static final boolean debug = Settings.debug;

    public static ICodec getAudioCodec(AudioCodecType audioCodecType) {
        switch (audioCodecType) {
            case Sit_2_1_java:
            default:
                Log.i(TAG, "AudioCodec audioCodecType default Sit_2_1_java");
                return new SitAudioCodec_2_1_java();
        }
    }

}
