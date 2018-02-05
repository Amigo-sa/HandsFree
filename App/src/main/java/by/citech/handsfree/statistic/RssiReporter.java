package by.citech.handsfree.statistic;

import android.os.Handler;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

public class RssiReporter {

    private final static String STAG = Tags.RssiReporter;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final int MIN_INTERVAL = 500;

    private int interval;
    private Handler handler;
    private IOnRssiUpdateListener listener;
    private IRssiProvider provider;

    private Runnable requestRssi = () -> {
        if (provider != null) provider.requestRssi();
    };

    //--------------------- singleton

    private static volatile RssiReporter instance = null;

    private RssiReporter() {
        interval = MIN_INTERVAL;
    }

    public static RssiReporter getInstance() {
        if (instance == null) {
            synchronized (RssiReporter.class) {
                if (instance == null) {instance = new RssiReporter();}}}
        return instance;
    }

    //--------------------- getters and setters

    public RssiReporter setListener(IOnRssiUpdateListener listener) {
        this.listener = listener;
        return this;
    }

    public RssiReporter setInterval(int interval) {
        if (interval > MIN_INTERVAL) this.interval = interval;
        return this;
    }

    public RssiReporter setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    //--------------------- main

    private void registerRssiProvider(IRssiProvider provider) {
        if (!isReady()) return;
        handler.postDelayed(requestRssi, interval);
        this.provider = provider;
    }

    private void unregisterRssiProvider(IRssiProvider provider) {
        if (!isReady()) return;
        handler.removeCallbacks(requestRssi);
        if (this.provider == provider) this.provider = null;
    }

    //--------------------- main

    public void onRssiResponse(int rssi) {
        if (!isReady()) return;
        handler.post(() -> listener.onRssiUpdated(rssi));
        handler.postDelayed(requestRssi, interval);
    }

    //--------------------- main

    private boolean isReady() {
        return handler != null && listener != null;
    }

    //--------------------- interfaces

    public interface IOnRssiUpdateListener {
        void onRssiUpdated(int rssi);
    }

    public interface IRssiProvider {
        void requestRssi();
    }

    public interface IRssiReporter {
        void onRssiResponse(int rssi);
    }

    public interface IRssiProviderRegister {

        default void registerRssiProvider(IRssiProvider provider) {
            RssiReporter.getInstance().registerRssiProvider(provider);
        }

        default void unregisterRssiProvider(IRssiProvider provider) {
            RssiReporter.getInstance().unregisterRssiProvider(provider);
        }

    }

}
