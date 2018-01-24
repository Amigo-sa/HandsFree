package by.citech.handsfree.exchange;

import android.util.Log;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface IRxComplex {

    String TAG = Tags.ITransmitter;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;

    default void sendMessage(String message) {
        Log.e(TAG, "sendMessage" + MSG);
    }

    default void sendData(byte[] data) {
        Log.e(TAG, "sendData byte[]" + MSG);
    }

    default void sendData(short[] data) {
        Log.e(TAG, "sendData short[]" + MSG);
    }

}
