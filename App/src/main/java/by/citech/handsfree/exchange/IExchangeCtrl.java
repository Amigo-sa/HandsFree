package by.citech.handsfree.exchange;

public interface IExchangeCtrl {
    ITransmitter getTransmitter();
    void setReceiver(ITransmitter iTransmitter);
}
