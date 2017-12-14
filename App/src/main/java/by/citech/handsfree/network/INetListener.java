package by.citech.handsfree.network;

public interface INetListener {
    void srvOnClose();
    void srvOnOpen();
    void srvOnFailure();
    void cltOnOpen();
    void cltOnMessageText(String message);
    void cltOnClose();
    void cltOnFailure();
}
