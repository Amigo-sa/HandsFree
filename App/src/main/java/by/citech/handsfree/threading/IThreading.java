package by.citech.handsfree.threading;

public interface IThreading {
    default boolean addRunnable(Runnable runnable) {
        return ThreadingManager.getInstance().addRunnable(runnable);
    }
}
