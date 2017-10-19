package by.citech.websocketduplex.client.network;

public interface IClientCtrl {
    void sendBytes(byte... bytes);
    String getStatus();
    void cancel();
    void run();
    void stop(String reason);
    void sendMessage(String string);
}
