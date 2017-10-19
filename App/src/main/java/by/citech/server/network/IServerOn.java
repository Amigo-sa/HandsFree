package by.citech.websocketduplex.server.network;

public interface IServerOn {
    void serverStarted(IServerCtrl iServerCtrl);
    void serverCantStart();
}
