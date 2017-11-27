package by.citech.exchange;

public interface ITransmitter {
    void sendMessage(String message);
    void sendData(byte[] data);
}
