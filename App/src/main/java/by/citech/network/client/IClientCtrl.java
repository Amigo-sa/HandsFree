package by.citech.network.client;

import by.citech.exchange.IExchangeCtrl;
import by.citech.network.control.IConnCtrl;

public interface IClientCtrl
        extends IExchangeCtrl, IConnCtrl {
    IClientCtrl startClient();
    String getStatus();
}
