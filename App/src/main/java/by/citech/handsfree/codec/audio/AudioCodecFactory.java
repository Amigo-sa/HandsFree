package by.citech.handsfree.codec.audio;

import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public abstract class AudioCodecFactory
        implements ICodec {

    private static final boolean debug = Settings.debug;

    public static ICodec getAudioCodec(EAudioCodecType audioCodecType) {
        switch (audioCodecType) {
            case Sit_3_0_java:
            case Sit_3_0_native:
                Timber.i("getAudioCodec audioCodecType Sit_3_0_java");
                return new SitAudioCodec_3_0_java();
            case Sit_2_1_java:
            case Sit_2_1_native:
            default:
                Timber.i("getAudioCodec audioCodecType default Sit_2_1_java");
                return new SitAudioCodec_2_1_java();
        }
    }

}
