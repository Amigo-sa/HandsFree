package by.citech.connection;

public interface ITransmitter {
    void sendMessage(String message);
    void sendBytes(byte... bytes);
}
