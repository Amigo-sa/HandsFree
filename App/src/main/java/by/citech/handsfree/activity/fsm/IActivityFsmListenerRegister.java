package by.citech.handsfree.activity.fsm;

import android.support.annotation.CallSuper;

public interface IActivityFsmListenerRegister {

    @CallSuper
    default boolean registerActivityFsmListener(IActivityFsmListener listener, String who) {
        return ActivityFsm.getInstance().registerListener(listener, who);
    }

    @CallSuper
    default boolean unregisterActivityFsmListener(IActivityFsmListener listener, String who) {
        return ActivityFsm.getInstance().unregisterListener(listener, who);
    }

}
