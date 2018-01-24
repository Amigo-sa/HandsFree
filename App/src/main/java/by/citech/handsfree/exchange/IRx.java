package by.citech.handsfree.exchange;

public interface IRx<T> {
    void onRx(T received);
    void onRxFinished();
}
