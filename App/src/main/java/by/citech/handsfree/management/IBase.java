package by.citech.handsfree.management;

import android.support.annotation.CallSuper;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface IBase {

    String TAG = Tags.I_BASE;
    String ERR_MSG = StatusMessages.ERR_NOT_OVERRIDED;

    @CallSuper
    default boolean baseStart() {
        return ResourceManager.getInstance().doBaseStart(this);
    }

    @CallSuper
    default boolean baseStop() {
        return ResourceManager.getInstance().doBaseStop(this);
    }

    @CallSuper
    default boolean baseCreate() {
        return ResourceManager.getInstance().doBaseCreate(this);
    }

    @CallSuper
    default boolean baseDestroy() {
        return ResourceManager.getInstance().doBaseDestroy(this);
    }

    @CallSuper
    default boolean basePause() {
        return false;
    }

    @CallSuper
    default boolean baseResume() {
        return false;
    }

}
