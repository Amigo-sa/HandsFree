package by.citech.handsfree.logic;

public interface ICallerFsmRegisterListener {

    default boolean registerCallerFsmListener(ICallerFsmListener listener, String who) {
        return CallerFsm.getInstance().registerListener(listener, who);
    }

    default boolean unregisterCallerFsmListener(ICallerFsmListener listener, String who) {
        return CallerFsm.getInstance().unregisterListener(listener, who);
    }

}
