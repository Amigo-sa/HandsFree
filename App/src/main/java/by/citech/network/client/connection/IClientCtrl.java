package by.citech.network.client.connection;

import by.citech.network.control.IExchangeCtrl;
import by.citech.network.control.IConnCtrl;

public interface IClientCtrl extends IExchangeCtrl, IConnCtrl {
    IClientCtrl startClient();
    String getStatus();
}
