package by.citech.handsfree.exchange;

import android.support.annotation.CallSuper;

public interface IExchangersManager {

    @CallSuper
    default void registerRxToExchangerManager(IRx<byte[]> receiver) {
        ExchangersManager.getInstance().registerRx(receiver);
    }

    @CallSuper
    default void registerTxToExchangerManager(ITx<byte[]> transmitter) {
        ExchangersManager.getInstance().registerTx(transmitter);
    }

    @CallSuper
    default void registerSubscriberToExchangerManager(ISubscriber<byte[]> subscriber) {
        ExchangersManager.getInstance().register(subscriber);
    }

}
