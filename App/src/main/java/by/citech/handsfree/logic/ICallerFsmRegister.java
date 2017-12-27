package by.citech.handsfree.logic;

public interface ICallerFsmRegister {

    default boolean registerCallerFsmListener(ICallerFsmListener listener) {
        return CallerFsm.getInstance().addListener(listener);
    }

}
