package by.citech.handsfree.gui;

public interface ICallToUiListener
        extends ICallToUiExchangeListener {
    void callOutcomingCanceled();
    void callOutcomingStarted();
    void callIncomingRejected();
}
