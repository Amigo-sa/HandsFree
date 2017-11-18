package by.citech.network.control;

public interface IConnCtrl
        extends IExchangeCtrl {
    void closeConnection();
    void closeConnectionForce();
    boolean isAliveConnection();
}
