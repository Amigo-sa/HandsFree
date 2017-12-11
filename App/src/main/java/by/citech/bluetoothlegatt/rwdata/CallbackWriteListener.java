package by.citech.bluetoothlegatt.rwdata;

/**
 * Created by tretyak on 17.11.2017.
 */

public interface CallbackWriteListener {
    void callbackIsDone();
    void rcvBtPktIsDone(byte[] data);
    void MtuChangedDone(int mtu);
    void callbackDescriptorIsDone();
}
