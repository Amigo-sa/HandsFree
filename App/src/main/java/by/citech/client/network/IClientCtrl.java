package by.citech.client.network;

import by.citech.connection.IReceiverRegister;
import by.citech.connection.ITransmitter;

public interface IClientCtrl {
    IClientCtrl run();
    void stop(String reason);
    void cancel();
    ITransmitter getTransmitter();
    IReceiverRegister getReceiverRegister();
    String getStatus();
}
