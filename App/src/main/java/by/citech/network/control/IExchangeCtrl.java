package by.citech.network.control;

import by.citech.network.receive.IReceiveListenerReg;
import by.citech.network.transmit.ITransmitter;

public interface IExchangeCtrl {
    ITransmitter getTransmitter();
    IReceiveListenerReg getReceiverRegister();
}
