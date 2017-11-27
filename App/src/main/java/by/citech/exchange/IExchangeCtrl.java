package by.citech.exchange;

import by.citech.exchange.IReceiveListenerReg;
import by.citech.exchange.ITransmitter;

public interface IExchangeCtrl {
    ITransmitter getTransmitter();
    IReceiveListenerReg getReceiverRegister();
}
