package by.citech.debug;

import android.util.Log;

import java.util.ArrayList;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class DebugTrafficAnalyzer extends Thread {

    private static final String TAG = Tags.TRAFFIC_STAT;
    private static final boolean debug = Settings.debug;

    //TODO: доделать
    private ArrayList<IDebugTraffic> iDebugTraffics;
    private ArrayList<ITrafficInfo> iTrafficInfos;
    private boolean isRunning;
    private long previousTimestamp, currentTimestamp, timeDelta;

    //--------------------- singleton

    private static volatile DebugTrafficAnalyzer instance = null;

    private DebugTrafficAnalyzer() {
        isRunning = false;
    }

    public static DebugTrafficAnalyzer getInstance() {
        if (instance == null) {
            synchronized (DebugTrafficAnalyzer.class) {
                if (instance == null) {
                    instance = new DebugTrafficAnalyzer();
                }
            }
        }
        return instance;
    }

    //--------------------- common

    public void addiDebugTraffic (IDebugTraffic iDebugTraffic) {
        iDebugTraffics.add(iDebugTraffic);
    }


    public void addiTrafficInfo(ITrafficInfo iTrafficInfo) {
        iTrafficInfos.add(iTrafficInfo);
    }

    //--------------------- main

    @Override
    public void run() {
        if (debug) Log.i(TAG, "run");
        isRunning = true;
        while (isRunning) {

        }
    }

    public void deactivate() {
        if (debug) Log.i(TAG, "deactivate");
        isRunning = false;
    }

}
