package by.citech.handsfree.connection.fsm;

import android.support.annotation.CallSuper;

public interface IConnectionFsmListenerRegister {

    @CallSuper
    default boolean registerConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return ConnectionFsm.getInstance().registerListener(listener, who);
    }

    @CallSuper
    default boolean unregisterConnectionFsmListener(IConnectionFsmListener listener, String who) {
        return ConnectionFsm.getInstance().unregisterListener(listener, who);
    }

}
