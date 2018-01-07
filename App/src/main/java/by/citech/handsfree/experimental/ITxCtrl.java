package by.citech.handsfree.experimental;

public interface ITxCtrl<T> {
    void registerTx(ITx<T> tx);
    void unregisterTx(ITx<T> tx);
}