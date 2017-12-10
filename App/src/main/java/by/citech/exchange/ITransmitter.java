package by.citech.exchange;

import android.util.Log;

import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public interface ITransmitter {

    String TAG = Tags.I_TRANSMITTER;

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
