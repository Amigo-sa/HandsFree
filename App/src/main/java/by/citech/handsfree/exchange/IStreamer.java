package by.citech.handsfree.exchange;

public interface IStreamer {
    void prepareStream(IRxComplex receiver) throws Exception;
    void streamOn();
    void streamOff();
    void finishStream();
    boolean isStreaming();
    boolean isReadyToStream();
}
