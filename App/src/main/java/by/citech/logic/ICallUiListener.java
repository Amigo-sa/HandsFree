package by.citech.logic;

public interface ICallUiListener {
    void callEndedInternally();
    void callOutcomingCanceled();
    void callOutcomingStarted();
    void callIncomingRejected();
    void callIncomingAccepted();// TODO: включение BT
}
