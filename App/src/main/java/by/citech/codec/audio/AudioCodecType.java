package by.citech.codec.audio;

public enum AudioCodecType {

    Sit_3_0_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
        @Override public String getSettingName() {return "Sit_3_0_native";}
    },

    Sit_3_0_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
        @Override public String getSettingName() {return "Sit_3_0_java";}
    },

    Sit_2_1_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
        @Override public String getSettingName() {return "Sit_2_1_native";}
    },

    Sit_2_1_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
        @Override public String getSettingName() {return "Sit_2_1_java";}
    };

    public abstract int getEncodedBytesSize();
    public abstract int getDecodedShortsSize();
    public abstract String getSettingName();
}
