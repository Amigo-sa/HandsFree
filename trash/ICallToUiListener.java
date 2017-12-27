package by.citech.handsfree.logic;

public interface ICallToUiListener
        extends ICallToUiExchangeListener {
    void callOutcomingCanceled();
    void callOutcomingStarted();
    void callIncomingRejected();
}
