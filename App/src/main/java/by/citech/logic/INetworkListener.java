package by.citech.logic;

public interface INetworkListener {
    void srvOnClose();
    void srvOnOpen();
    void srvOnFailure();
    void cltOnOpen();
    void cltOnMessageText(String message);
    void cltOnClose();
    void cltOnFailure();
}
