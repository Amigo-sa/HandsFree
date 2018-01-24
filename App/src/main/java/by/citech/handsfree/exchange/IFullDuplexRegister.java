package by.citech.handsfree.exchange;

interface IFullDuplexRegister<T> {
    void register(ISubscriber<T> subscriber);
}
