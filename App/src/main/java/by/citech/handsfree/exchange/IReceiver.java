package by.citech.handsfree.exchange;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IReceiver {
    String TAG = Tags.I_RECEIVER;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;
    default void onReceiveData(byte[] data) {Log.e(TAG, "onReceiveData byte[]" + MSG);}
    default void onReceiveData(short[] data) {Log.e(TAG, "onReceiveData short[]" + MSG);}
}
