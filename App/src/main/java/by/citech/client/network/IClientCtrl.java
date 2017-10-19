package by.citech.client.network;

public interface IClientCtrl {
    void sendBytes(byte... bytes);
    String getStatus();
    void cancel();
    IClientCtrl run();
    void stop(String reason);
    void sendMessage(String string);
}
