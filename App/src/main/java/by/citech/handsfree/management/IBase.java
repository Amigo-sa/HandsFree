package by.citech.handsfree.management;

import android.support.annotation.CallSuper;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface IBase {

    String TAG = Tags.IBase;
    String ERR_MSG = StatusMessages.ERR_NOT_OVERRIDED;

    @CallSuper
    default boolean baseStart() {
        return ResourceManager.getInstance().registerBaseStart(this);
    }

    @CallSuper
    default boolean baseStop() {
        return ResourceManager.getInstance().unregisterBaseStart(this);
    }

    @CallSuper
    default boolean baseCreate() {
        return ResourceManager.getInstance().registerBaseCreate(this);
    }

    @CallSuper
    default boolean baseDestroy() {
        return ResourceManager.getInstance().unregisterBaseCreate(this);
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
