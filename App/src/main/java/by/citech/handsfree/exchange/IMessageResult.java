package by.citech.handsfree.exchange;

public interface IMessageResult {
    default void messageSended() {};
    default void messageCantSend() {};
}
