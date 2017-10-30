package by.citech.logic;

public interface ICallNetworkListener {
    void callOutcomingConnected();
    void callOutcomingAccepted(); // TODO: включение BT
    void callOutcomingRejected();
    void callOutcomingFailed();
    void callOutcomingLocal();
    void callIncomingDetected();
    void callIncomingCanceled();
    void callIncomingFailed();
    void callFailed(); // TODO: выключение BT
    void callEndedExternally(); // TODO: выключение BT
    void connectorFailure();
    void connectorReady();
}
