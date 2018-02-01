package by.citech.handsfree.network.control;

import by.citech.handsfree.exchange.IExchangeCtrl;

public interface IConnCtrl extends IExchangeCtrl {
    void closeConnection();
    void closeConnectionForce();
    boolean isAliveConnection();
}
