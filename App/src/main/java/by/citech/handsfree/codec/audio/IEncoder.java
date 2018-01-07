package by.citech.handsfree.codec.audio;

public interface IEncoder {
    void initiateEncoder();
    byte[] getEncodedData(short[] dataToEncode);
}
