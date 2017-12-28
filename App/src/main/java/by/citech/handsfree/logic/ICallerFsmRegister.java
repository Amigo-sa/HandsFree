package by.citech.handsfree.logic;

public interface ICallerFsmRegister {

    default boolean registerCallerFsmListener(ICallerFsmListener listener, String who) {
        return CallerFsm.getInstance().addListener(listener, who);
    }

}
