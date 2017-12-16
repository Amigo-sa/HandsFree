package by.citech.handsfree.common;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IPrepareObject {

    String TAG = Tags.I_CHECK;
    String MSG_ERR = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean prepareObject() {
        Log.e(TAG, "prepareObject" + MSG_ERR);
        return false;
    }

    default boolean isObjectPrepared() {
        Log.e(TAG, "isObjectPrepared" + MSG_ERR);
        return false;
    }

}
