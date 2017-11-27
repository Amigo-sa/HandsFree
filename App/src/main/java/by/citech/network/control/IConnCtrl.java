package by.citech.network.control;

import by.citech.exchange.IExchangeCtrl;

public interface IConnCtrl
        extends IExchangeCtrl {
    void closeConnection();
    void closeConnectionForce();
    boolean isAliveConnection();
}
