package by.citech.handsfree.statistic;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class TrafficAnalyzer
        extends Thread {

    private static final String TAG = Tags.TrafficAnalyzer;
    private static final boolean debug = Settings.debug;
    private static final long SLEEP_INTERVAL = 500;

    //TODO: доделать
    private List<TrafficInfo> trafficInfos;
    private ITrafficReporter iTrafficReporter;
    private boolean isRunning;
    private long prevTimestamp, currTimestamp, timeDelta;

    //--------------------- singleton

    private static volatile TrafficAnalyzer instance = null;

    private TrafficAnalyzer() {
        isRunning = false;
        prevTimestamp = 0;
        currTimestamp = 0;
        timeDelta = 0;
        trafficInfos = new ArrayList<>();
    }

    public static TrafficAnalyzer getInstance() {
        if (instance == null) {
            synchronized (TrafficAnalyzer.class) {
                if (instance == null) {
                    instance = new TrafficAnalyzer();
                }
            }
        }
        return instance;
    }

    //--------------------- common

    public void addTrafficInfo(TrafficInfo trafficInfo) {
        trafficInfos.add(trafficInfo);
        if (iTrafficReporter != null) {
            iTrafficReporter.publishTrafficInfo(trafficInfo);
        }
    }

    public void setiTrafficReporter(ITrafficReporter iTrafficReporter) {
        this.iTrafficReporter = iTrafficReporter;
    }

    //--------------------- main

    @Override
    public void run() {
        if (iTrafficReporter == null) {
            Timber.e("run one of key parameters are null");
            return;
        }
        Timber.i("run");
        isRunning = true;
        while (isRunning) {
            currTimestamp = System.currentTimeMillis();
            if (prevTimestamp != 0) {
                timeDelta = currTimestamp - prevTimestamp;
                for (TrafficInfo trafficInfo : trafficInfos) {
                    trafficInfo.updateInfo(timeDelta);
                }
                iTrafficReporter.updateTrafficInfo();
            }
            prevTimestamp = currTimestamp;
            try {
                Thread.sleep(SLEEP_INTERVAL);
                if (!isRunning) return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void deactivate() {
        Timber.i("deactivate");
        isRunning = false;
    }

    public interface ITrafficReporter {
        void publishTrafficInfo(TrafficInfo trafficInfo);
        void updateTrafficInfo();
    }

    public interface ITrafficUpdate {
        default Long getBytesDelta() {return null;};
    }

}
