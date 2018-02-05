package by.citech.handsfree.codec;

public interface IDecoder {
    void initiateDecoder();
    short[] getDecodedData(byte[] dataToDecode);
}
