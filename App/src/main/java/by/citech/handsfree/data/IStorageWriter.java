package by.citech.handsfree.data;

public interface IStorageWriter<T> {
    void putData(T dataIn);
    void clear();
}
