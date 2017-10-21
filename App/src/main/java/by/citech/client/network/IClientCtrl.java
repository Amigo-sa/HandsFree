package by.citech.client.network;

import by.citech.connection.IReceiverListenerRegister;
import by.citech.connection.ITransmitter;

public interface IClientCtrl extends ITransmitter, IReceiverListenerRegister {
    IClientCtrl run();
    void stop(String reason);
    void cancel();
    ITransmitter getTransmitter();
    IReceiverListenerRegister getReceiverRegister();
    String getStatus();
}
