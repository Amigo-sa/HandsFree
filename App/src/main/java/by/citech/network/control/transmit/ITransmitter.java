package by.citech.network.control.transmit;

public interface ITransmitter {
    void sendMessage(String message);
    void sendData(byte[] data);
}
