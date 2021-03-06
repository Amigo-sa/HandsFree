package by.citech.handsfree.codec.audio;

import by.citech.handsfree.settings.ISettingEnum;
import by.citech.handsfree.settings.SettingsDefault;

public enum EAudioCodecType implements ISettingEnum<EAudioCodecType> {

    Sit_2_1_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
    },

    Sit_2_1_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
    },

    Sit_3_0_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
    },

    Sit_3_0_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
    };

    @Override public String getSettingName() {return this.name();}
    @Override public String getSettingNumber() {return String.valueOf(this.ordinal() + 1);}
    @Override public String getDefaultName() {return getDefaultValue().getSettingName();};
    @Override public String getTypeName() {return SettingsDefault.TypeName.audioCodecType;};
    @Override public EAudioCodecType getDefaultValue() {return SettingsDefault.AudioCommon.audioCodecType;}
    @Override public EAudioCodecType[] getValues() {return values();}

    public abstract int getEncodedBytesSize();
    public abstract int getDecodedShortsSize();

}
