package by.citech.debug;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class TrafficAnalyzer extends Thread {

    private static final String TAG = Tags.TRAFFIC_ANAL;
    private static final boolean debug = Settings.debug;
    private static final long SLEEP_INTERVAL = 500;

    //TODO: доделать
    private List<TrafficInfo> trafficInfos;
    private ITrafficReporter iTrafficReporter;
    private boolean isRunning;
    private long previousTimestamp, currentTimestamp, timeDelta;

    //--------------------- singleton

    private static volatile TrafficAnalyzer instance = null;

    private TrafficAnalyzer() {
        isRunning = false;
        previousTimestamp = 0;
        currentTimestamp = 0;
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
            Log.e(TAG, "run one of key parameters are null");
            return;
        }
        if (debug) Log.i(TAG, "run");
        isRunning = true;
        while (isRunning) {
            currentTimestamp = System.currentTimeMillis();
            if (previousTimestamp != 0) {
                timeDelta = currentTimestamp - previousTimestamp;
                for (TrafficInfo trafficInfo : trafficInfos) {
                    trafficInfo.updateInfo(timeDelta);
                }
                iTrafficReporter.updateTrafficInfo();
            }
            previousTimestamp = currentTimestamp;
            try {
                Thread.sleep(SLEEP_INTERVAL);
                if (!isRunning) return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isRunning = false;
    }

}
