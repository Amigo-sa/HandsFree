package by.citech.codec.audio;

public enum AudioCodecType {

    Sit {
        @Override public int getDecodedShortCnt() {return 80;}
        @Override public int getEncodedByteCnt() {return 10;}
    };

    public abstract int getEncodedByteCnt();
    public abstract int getDecodedShortCnt();
}
