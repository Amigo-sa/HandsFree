package by.citech.handsfree.codec;

public interface IEncoder {
    void initiateEncoder();
    byte[] getEncodedData(short[] dataToEncode);
}
