package by.citech.handsfree.exchange;

public interface ITx<T> {
    void registerRx(IRx<T> receiver);
    void unregisterRx(IRx<T> receiver);
}