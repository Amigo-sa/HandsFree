package by.citech.codec.audio;

import by.citech.param.IEnumSetting;
import by.citech.param.SettingsDefault;

public enum AudioCodecType implements IEnumSetting<AudioCodecType> {

    Sit_2_1_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
        @Override public String getSettingName() {return "Sit_2_1_java";}
        @Override public String getSettingNumber() {return "1";}
    },

    Sit_2_1_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
        @Override public String getSettingName() {return "Sit_2_1_native";}
        @Override public String getSettingNumber() {return "2";}
    },

    Sit_3_0_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
        @Override public String getSettingName() {return "Sit_3_0_java";}
        @Override public String getSettingNumber() {return "3";}
    },

    Sit_3_0_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
        @Override public String getSettingName() {return "Sit_3_0_native";}
        @Override public String getSettingNumber() {return "4";}
    };

    @Override
    public String getDefaultSettingName() {
        return getDefaultSetting().getSettingName();
    };

    @Override
    public String getSettingKey() {
        return SettingsDefault.Key.audioCodecType;
    };

    @Override
    public AudioCodecType getDefaultSetting() {
        return SettingsDefault.AudioCommon.audioCodecType;
    }

    public abstract int getEncodedBytesSize();
    public abstract int getDecodedShortsSize();

}
