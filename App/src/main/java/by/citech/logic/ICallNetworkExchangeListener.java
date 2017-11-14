package by.citech.logic;

public interface ICallNetworkExchangeListener {
    void callOutcomingAccepted(); // TODO: включение BT
    void callFailed(); // TODO: выключение BT
    void callEndedExternally(); // TODO: выключение BT
}
