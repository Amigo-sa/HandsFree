package by.citech.handsfree.network.client;

import by.citech.handsfree.exchange.IExchangeCtrl;
import by.citech.handsfree.network.control.IConnCtrl;

public interface IClientCtrl
        extends IExchangeCtrl, IConnCtrl {
    IClientCtrl startClient();
    String getStatus();
}
