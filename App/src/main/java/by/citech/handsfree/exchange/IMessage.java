package by.citech.handsfree.exchange;

public interface IMessage {
    default void messageSended() {};
    default void messageCantSend() {};
}
