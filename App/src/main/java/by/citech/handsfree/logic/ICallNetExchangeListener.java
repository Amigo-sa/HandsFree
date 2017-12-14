package by.citech.handsfree.logic;

public interface ICallNetExchangeListener {
    void callOutcomingAccepted(); // TODO: включение BT
    void callFailed(); // TODO: выключение BT
    void callEndedExternally(); // TODO: выключение BT
}
