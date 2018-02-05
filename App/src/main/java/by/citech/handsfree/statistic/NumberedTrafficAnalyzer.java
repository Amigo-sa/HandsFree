package by.citech.handsfree.statistic;

import android.os.Handler;
import android.support.annotation.CallSuper;
import android.util.Log;

import java.util.Arrays;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

import static by.citech.handsfree.util.MathHelper.convertByteArrToIntRaw;

public class NumberedTrafficAnalyzer {

    private final static String STAG = Tags.NumberedTrafficAnalyzer;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private static final int MIN_INTERVAL = 500;
    private static final int from = Settings.Bluetooth.btNumberedBytesToIntStart;
    private static final int to = from + 4;

    private boolean isDeactivated;

    private int packetSize;
    private int expectedInt;
    private int actualInt;

    private long maxLostPacketsAmount;
    private long totalPacketsCount;
    private long lastLostPacketsAmount;
    private long totalReceivedPacketsCount;
    private long totalLostPacketsCount;
    private long deltaPacketsCount;
    private long deltaLostPacketsCount;
    private long totalBytesCount;
    private double totalBytesPerSec;
    private double deltaBytesPerSec;
    private double totalLostPercent;
    private double deltaLostPercent;

    private NumberedTrafficInfo info;
    private IOnInfoUpdateListener listener;
    private long deltaTime;
    private long prevTimestamp;
    private long currTimestamp;
    private long totalTime;
    private int interval;
    private Handler handler;

    private Runnable postToPost = () -> {
        updateInfo();
        if (listener != null) listener.onNumberedTrafficInfoUpdated(info);
        postThePost();
    };

    //--------------------- singleton

    private static volatile NumberedTrafficAnalyzer instance = null;

    private NumberedTrafficAnalyzer() {
        interval = MIN_INTERVAL;
        info = new NumberedTrafficInfo();
    }

    public static NumberedTrafficAnalyzer getInstance() {
        if (instance == null) {
            synchronized (NumberedTrafficAnalyzer.class) {
                if (instance == null) {
                    instance = new NumberedTrafficAnalyzer();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public NumberedTrafficAnalyzer setInterval(int interval) {
        if (interval > MIN_INTERVAL) this.interval = interval;
        return this;
    }

    public NumberedTrafficAnalyzer setListener(IOnInfoUpdateListener listener) {
        this.listener = listener;
        return this;
    }

    public NumberedTrafficAnalyzer setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    //--------------------- analyze

    void analyzeNumberedBytes(byte[] traffic) {
        if (!debug || traffic == null) return;
        actualInt = convertByteArrToIntRaw(Arrays.copyOfRange(traffic, from, to));
        if (!isDeactivated) {
            if (expectedInt != actualInt) {
                if (expectedInt > actualInt) {
                    lastLostPacketsAmount = expectedInt - actualInt;
                } else {
                    lastLostPacketsAmount = actualInt - expectedInt;
                }
                totalLostPacketsCount += lastLostPacketsAmount;
                totalPacketsCount     += lastLostPacketsAmount;
                deltaPacketsCount     += lastLostPacketsAmount;
                deltaLostPacketsCount += lastLostPacketsAmount;
            } else {
                lastLostPacketsAmount = 0;
            }
            if (lastLostPacketsAmount != 0) {
                logError(actualInt, lastLostPacketsAmount);
            } else {
                logOk(actualInt);
            }
            if (lastLostPacketsAmount > maxLostPacketsAmount) {
                maxLostPacketsAmount = lastLostPacketsAmount;
            }
        } else {
            isDeactivated = false;
            prevTimestamp = System.currentTimeMillis();
            packetSize = traffic.length;
            postThePost();
        }
        totalReceivedPacketsCount++;
        totalPacketsCount++;
        deltaPacketsCount++;
        expectedInt = actualInt + 1;
    }

    void resetStatistic() {
        if (handler != null) handler.removeCallbacks(postToPost);
        isDeactivated = true;
        packetSize = 0;
        totalLostPacketsCount = 0;
        maxLostPacketsAmount = 0;
        expectedInt = 0;
        actualInt = 0;
        totalReceivedPacketsCount = 0;
        totalPacketsCount = 0;
        lastLostPacketsAmount = 0;
        deltaTime = 0;
        prevTimestamp = 0;
        currTimestamp = 0;
        totalTime = 0;
        totalBytesCount = 0;
        totalBytesPerSec = 0;
        totalLostPercent = 0;
        deltaLostPacketsCount = 0;
        deltaLostPercent = 0;
        deltaBytesPerSec = 0;
    }

    //--------------------- updating

    private void updateInfo() {
        if (isDeactivated) return;
        currTimestamp = System.currentTimeMillis();
        deltaTime = currTimestamp - prevTimestamp;
        totalTime += deltaTime;
        prevTimestamp = currTimestamp;
        totalBytesCount = totalPacketsCount * packetSize;
        totalBytesPerSec = ((double) totalBytesCount * 1000.0D) / (double) totalTime;
        totalLostPercent = ((double) totalLostPacketsCount * 100.0D) / (double) totalPacketsCount;
        deltaLostPercent = ((double) deltaLostPacketsCount * 100.0D) / (double) deltaPacketsCount;
        deltaBytesPerSec = ((double) deltaPacketsCount * (double) packetSize * 1000.0D) / (double) deltaTime;

        info.update(
                packetSize,
                lastLostPacketsAmount,
                maxLostPacketsAmount,
                totalPacketsCount,
                totalReceivedPacketsCount,
                totalLostPacketsCount,
                totalBytesPerSec,
                totalBytesCount,
                totalLostPercent,
                deltaLostPacketsCount,
                deltaLostPercent,
                deltaBytesPerSec
        );

        deltaTime = 0;
        deltaBytesPerSec = 0;
        deltaLostPacketsCount = 0;
        deltaLostPercent = 0;
        deltaPacketsCount = 0;
    }

    //--------------------- additional

    private void logError(int actualInt, long lastLost) {
        Timber.e("analyzeNumberedBytes: приняли <%08x>, ожидали <%08x>, потеряно %d, всего потеряно %d",
                actualInt, expectedInt, lastLost, totalPacketsCount);
    }

    private void logOk(int actualInt) {
        Timber.w("analyzeNumberedBytes: приняли %08x", actualInt);
    }

    private void postThePost() {
        if (handler != null) handler.postDelayed(postToPost, interval);
    }

    //--------------------- interfaces

    public interface IOnInfoUpdateListener {
        void onNumberedTrafficInfoUpdated(NumberedTrafficInfo updatedInfo);
    }

    public interface INumberedTrafficAnalyzer {

        @CallSuper
        default void resetStatistic() {
            NumberedTrafficAnalyzer.getInstance().resetStatistic();
        }

        @CallSuper
        default void analyzeNumberedBytes(byte[] lastReceived) {
            NumberedTrafficAnalyzer.getInstance().analyzeNumberedBytes(lastReceived);
        }

    }

}
