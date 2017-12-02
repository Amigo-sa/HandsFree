package by.citech.codec.audio;

public enum AudioCodecType {

    Sit_3_0_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
    },

    Sit_3_0_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 20;}
    },

    Sit_2_1_native {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
    },

    Sit_2_1_java {
        @Override public int getDecodedShortsSize() {return 80;}
        @Override public int getEncodedBytesSize() {return 10;}
    };

    public abstract int getEncodedBytesSize();
    public abstract int getDecodedShortsSize();
}
