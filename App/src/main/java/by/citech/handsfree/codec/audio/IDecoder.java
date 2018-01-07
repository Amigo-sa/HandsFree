package by.citech.handsfree.codec.audio;

public interface IDecoder {
    void initiateDecoder();
    short[] getDecodedData(byte[] dataToDecode);
}
