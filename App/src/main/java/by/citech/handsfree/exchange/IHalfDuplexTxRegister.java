package by.citech.handsfree.exchange;

public interface IHalfDuplexTxRegister<T> {
    void registerTx(ITx<T> transmitter);
}
