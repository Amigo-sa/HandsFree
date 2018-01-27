package by.citech.handsfree.experimental.fsm;

import android.support.annotation.CallSuper;

public interface IConnectionFsmListenerRegister {

    @CallSuper
    default boolean registerConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return ConnectionFsm.getInstance().registerConnectionFsmListener(listener, who);
    }

    @CallSuper
    default boolean unregisterConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return ConnectionFsm.getInstance().unregisterConnectionFsmListener(listener, who);
    }

}
