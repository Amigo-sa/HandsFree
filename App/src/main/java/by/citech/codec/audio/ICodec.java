package by.citech.codec.audio;

public interface ICodec {
    void initiateDecoder();
    void initiateEncoder();
    short[] getDecodedData(byte[] dataToDecode);
    byte[] getEncodedData(short[] dataToEncode);
}
