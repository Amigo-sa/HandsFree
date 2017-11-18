package by.citech.gui;

public interface ICallUiListener extends ICallUiExchangeListener {
    void callOutcomingCanceled();
    void callOutcomingStarted();
    void callIncomingRejected();
}
