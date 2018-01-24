package by.citech.handsfree.exchange;

public interface IHalfDuplexRxRegister<T> {
    void registerRx(IRx<T> receiver);
}
