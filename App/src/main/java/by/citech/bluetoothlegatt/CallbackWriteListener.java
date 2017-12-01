package by.citech.bluetoothlegatt;

/**
 * Created by tretyak on 17.11.2017.
 */

public interface CallbackWriteListener {
    void callbackIsDone();
    void rcvBtPktIsDone(byte[] data);
}
