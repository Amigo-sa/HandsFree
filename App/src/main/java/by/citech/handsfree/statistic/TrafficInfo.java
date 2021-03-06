package by.citech.handsfree.statistic;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class TrafficInfo {

    private static final String TAG = Tags.TrafficInfo;
    private static final boolean debug = Settings.debug;
    private static final long customPeriod = 2000;
    private static final long msToSFactor = 1000;

    private long averageBytesPerSec;
    private long averageParamPerSec;
    private long customIntervalBytesPerSec;
    private long customIntervalParamPerSec;
    private long overallMs;
    private long overallBytes;
    private ETrafficNodes node;
    private TrafficAnalyzer.ITrafficUpdate iTrafficUpdate;
    private boolean isInitiated;

    public TrafficInfo(ETrafficNodes node, TrafficAnalyzer.ITrafficUpdate iTrafficUpdate) {
        if (node == null || iTrafficUpdate == null) {
            return;
        }
        this.node = node;
        this.iTrafficUpdate = iTrafficUpdate;
        averageBytesPerSec = 0;
        averageParamPerSec = 0;
        customIntervalBytesPerSec = 0;
        customIntervalParamPerSec = 0;
        overallMs = 0;
        isInitiated = false;
    }

    public long getCustomBytesPerSec() {
        return customIntervalBytesPerSec;
    }

    public long getAverageBytesPerSec() {
        return averageBytesPerSec;
    }

    public long getCustomIntervalParamPerSec() {
        return customIntervalParamPerSec;
    }

    public long getAverageParamPerSec() {
        return averageParamPerSec;
    }

    public ETrafficNodes getNode() {
        return node;
    }

    public void updateInfo(long timeDelta) {
        if (!isInitiated) {
            isInitiated = true;
            iTrafficUpdate.getBytesDelta();
            return;
        }
        long bytesDelta = iTrafficUpdate.getBytesDelta();
        overallBytes = overallBytes + bytesDelta;
        overallMs = overallMs + timeDelta;
        averageBytesPerSec = (overallBytes * msToSFactor) / overallMs;
        customIntervalBytesPerSec = (bytesDelta * customPeriod) / timeDelta;
        //TODO: доделать
        averageParamPerSec = averageBytesPerSec;
    }

}
