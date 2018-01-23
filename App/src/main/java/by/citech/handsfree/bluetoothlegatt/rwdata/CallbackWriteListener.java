package by.citech.handsfree.bluetoothlegatt.rwdata;

public interface CallbackWriteListener {
    void callbackIsDone();
    void rcvBtPktIsDone(byte[] data);
    void onMtuChangeIsDone(int mtu);
    void callbackDescriptorIsDone();
}
