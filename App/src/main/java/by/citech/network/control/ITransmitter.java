package by.citech.network.control;

public interface ITransmitter {
    void sendMessage(String message);
    void sendBytes(byte... bytes);
}
