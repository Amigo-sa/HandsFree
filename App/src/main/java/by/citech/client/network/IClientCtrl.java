package by.citech.client.network;

import by.citech.connection.IExchangeCtrl;
import by.citech.connection.IConnCtrl;

public interface IClientCtrl extends IExchangeCtrl, IConnCtrl {
    IClientCtrl startClient();
    String getStatus();
}
