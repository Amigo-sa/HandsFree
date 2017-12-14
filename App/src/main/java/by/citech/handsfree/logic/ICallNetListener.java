package by.citech.handsfree.logic;

public interface ICallNetListener
        extends ICallNetExchangeListener {
    void callOutcomingConnected();
    void callOutcomingRejected();
    void callOutcomingFailed();
    void callOutcomingInvalid();
    void callIncomingDetected();
    void callIncomingCanceled();
    void callIncomingFailed();
    void connectorFailure();
    void connectorReady();
}
