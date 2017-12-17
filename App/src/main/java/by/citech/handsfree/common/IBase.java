package by.citech.handsfree.common;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IBase {

    String TAG = Tags.I_BASE;
    String ERR_MSG = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean baseStart() {
        return ResourceManager.getInstance().addBase(this);
    }

    default boolean baseStop() {
        return ResourceManager.getInstance().removeBase(this);
    }

    default void baseDestroy() {
        Log.e(TAG, "baseDestroy" + ERR_MSG);
    }

    default void basePause() {
        Log.e(TAG, "basePause" + ERR_MSG);
    }

    default void baseResume() {
        Log.e(TAG, "baseResume" + ERR_MSG);
    }

}
