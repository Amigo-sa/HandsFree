package by.citech.logic;

public interface ICallNetListener
        extends ICallNetExchangeListener {
    void callOutcomingConnected();
    void callOutcomingRejected();
    void callOutcomingFailed();
    void callOutcomingLocal();
    void callIncomingDetected();
    void callIncomingCanceled();
    void callIncomingFailed();
    void connectorFailure();
    void connectorReady();
}
