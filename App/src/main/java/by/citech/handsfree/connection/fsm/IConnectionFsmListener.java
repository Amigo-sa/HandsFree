package by.citech.handsfree.connection.fsm;

public interface IConnectionFsmListener {
    void onConnectionFsmStateChange(EConnectionState from, EConnectionState to, EConnectionReport why);
}
