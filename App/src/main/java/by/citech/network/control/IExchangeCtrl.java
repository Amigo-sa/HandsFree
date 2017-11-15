package by.citech.network.control;

public interface IExchangeCtrl {
    ITransmitter getTransmitter();
    IReceiveListenerReg getReceiverRegister();
}
