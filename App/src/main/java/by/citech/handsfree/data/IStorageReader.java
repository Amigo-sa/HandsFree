package by.citech.handsfree.data;

public interface IStorageReader<T> {
    void setWriteLocked(boolean isLocked);
    boolean isEmpty();
    T getData();
}
