package by.citech.handsfree.exchange;

public interface ITransmitterCtrl {
    void prepareStream(ITransmitter receiver) throws Exception;
    void streamOn();
    void streamOff();
    void finishStream();
    boolean isStreaming();
    boolean isReadyToStream();
}
