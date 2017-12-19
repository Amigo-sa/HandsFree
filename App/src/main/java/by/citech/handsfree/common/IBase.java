package by.citech.handsfree.common;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IBase {

    String TAG = Tags.I_BASE;
    String ERR_MSG = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean baseCreate() {
        return ResourceManager.getInstance().doBaseCreate(this);
    }

    default boolean baseStart() {
        return ResourceManager.getInstance().doBaseStart(this);
    }

    default boolean baseStop() {
        return ResourceManager.getInstance().doBaseStop(this);
    }

    default boolean baseDestroy() {
        return ResourceManager.getInstance().doBaseDestroy(this);
    }

    default boolean basePause() {
        Log.e(TAG, "basePause" + ERR_MSG);
        return false;
    }

    default boolean baseResume() {
        Log.e(TAG, "baseResume" + ERR_MSG);
        return false;
    }

}
