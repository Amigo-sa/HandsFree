package by.citech.logic;

public interface ICallUiListener extends ICallUiExchangeListener {
    void callOutcomingCanceled();
    void callOutcomingStarted();
    void callIncomingRejected();
}
