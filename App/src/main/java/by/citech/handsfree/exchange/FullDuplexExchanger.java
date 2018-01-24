package by.citech.handsfree.exchange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class FullDuplexExchanger<T>
        extends AbstractExchanger
        implements IFullDuplexRegister<T> {

    private Set<ISubscriber<T>> subscribers;

    {
        subscribers = new HashSet<>();
    }

    @Override
    synchronized public void register(ISubscriber<T> subscriber) {
        if (subscriber == null) return;
        subscribers.add(subscriber);
        process();
    }

    @Override
    protected void process() {
        if (isReady()) {
            Iterator<ISubscriber<T>> i = subscribers.iterator();
            ISubscriber<T> a = i.next();
            ISubscriber<T> b = i.next();
            a.registerRx(b);
            b.registerRx(a);
            reset();
        } else {
            setFinished(false);
        }
    }

    @Override
    void reset() {
        subscribers.clear();
        setFinished(true);
    }

    @Override
    protected boolean isReady() {
        return subscribers.size() == 2;
    }

}
