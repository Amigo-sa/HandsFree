package by.citech.exchange;

public interface IReceiver {
    void onReceiveData(byte[] data);
    void onReceiveData(short[] data);
}
