package by.citech.handsfree.experimental;

public interface ITx<T> {
    void tx(T t);
    void onTxFinished();
}
