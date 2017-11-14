package by.citech.logic;

public interface ICallNetworkListener extends ICallNetworkExchangeListener {
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
