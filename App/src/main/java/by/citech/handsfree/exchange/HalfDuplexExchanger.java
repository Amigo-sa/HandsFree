package by.citech.handsfree.exchange;

public final class HalfDuplexExchanger<T>
        extends AbstractExchanger
        implements IHalfDuplexRxRegister<T>, IHalfDuplexTxRegister<T> {

    private IRx<T> rx;
    private ITx<T> tx;

    @Override
    public void registerRx(IRx<T> receiver) {
        if (receiver != null) rx = receiver;
        process();
    }

    @Override
    public void registerTx(ITx<T> transmitter) {
        if (transmitter != null) tx = transmitter;
        process();
    }

    @Override
    protected void process() {
        if (isReady()) {
            tx.registerRx(rx);
            reset();
        } else {
            setFinished(false);
        }
    }

    @Override
    void reset() {
        rx = null;
        tx = null;
        setFinished(true);
    }

    @Override
    protected boolean isReady() {
        return rx != null && tx != null;
    }

}
