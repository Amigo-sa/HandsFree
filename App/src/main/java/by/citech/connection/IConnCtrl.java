package by.citech.connection;

public interface IConnCtrl extends IExchangeCtrl {
    void closeConnection();
    void closeConnectionForce();
    boolean isAliveConnection();
}
