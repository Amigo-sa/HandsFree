package by.citech.handsfree.exchange;

class ExchangersManager {

    private final static String FULL = "full-duplex";
    private final static String HALF = "half-duplex";

    private FullDuplexExchanger<byte[]> full;
    private HalfDuplexExchanger<byte[]> half;

    private static volatile ExchangersManager instance = null;

    private ExchangersManager() {
    }

    static ExchangersManager getInstance() {
        if (instance == null) {
            synchronized (ExchangersManager.class) {
                if (instance == null) {
                    instance = new ExchangersManager();
                }
            }
        }
        return instance;
    }

    void registerRx(IRx<byte[]> receiver) {
        initHalfDuplex();
        half.registerRx(receiver);
    }

    void registerTx(ITx<byte[]> transmitter) {
        initHalfDuplex();
        half.registerTx(transmitter);
    }

    void register(ISubscriber<byte[]> subscriber) {
        initFullDuplex();
        full.register(subscriber);
    }

    private void initHalfDuplex() {
        if (half == null) {
            half = new HalfDuplexExchanger<>();
            half.setTag(HALF);
        }
    }

    private void initFullDuplex() {
        if (full == null) {
            full = new FullDuplexExchanger<>();
            full.setTag(FULL);
        }
    }

}
