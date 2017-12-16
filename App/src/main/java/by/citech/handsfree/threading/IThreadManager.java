package by.citech.handsfree.threading;

public interface IThreadManager {
    default boolean addRunnable(Runnable runnable) {
        return ThreadManager.getInstance().addRunnable(runnable);
    }
}
