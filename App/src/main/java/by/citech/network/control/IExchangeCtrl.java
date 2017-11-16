package by.citech.network.control;

import by.citech.network.control.receive.IReceiveListenerReg;
import by.citech.network.control.transmit.ITransmitter;

public interface IExchangeCtrl {
    ITransmitter getTransmitter();
    IReceiveListenerReg getReceiverRegister();
}
