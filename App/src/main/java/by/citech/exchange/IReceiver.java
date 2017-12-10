package by.citech.exchange;

import android.util.Log;

import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public interface IReceiver {
    String TAG = Tags.I_RECEIVER;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;
    default void onReceiveData(byte[] data) {Log.e(TAG, "onReceiveData byte[]" + MSG);}
    default void onReceiveData(short[] data) {Log.e(TAG, "onReceiveData short[]" + MSG);}
}
