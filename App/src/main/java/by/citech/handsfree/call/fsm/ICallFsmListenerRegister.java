package by.citech.handsfree.call.fsm;

public interface ICallFsmListenerRegister {

    default boolean registerCallerFsmListener(ICallFsmListener listener, String who) {
        return CallFsm.getInstance().registerListener(listener, who);
    }

    default boolean unregisterCallerFsmListener(ICallFsmListener listener, String who) {
        return CallFsm.getInstance().unregisterListener(listener, who);
    }

}
