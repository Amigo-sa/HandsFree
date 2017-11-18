package by.citech.network.transmit;

public interface ITransmitter {
    void sendMessage(String message);
    void sendData(byte[] data);
}
