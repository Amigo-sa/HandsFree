package by.citech.debug;

public class TrafficInfo {

    private long averageBytesPerSec;
    private long averageMsPerSec;
    private long tenSecBytesPerSec;
    private long tenSecMsPerSec;
    private TrafficNodes node;

    public TrafficInfo(TrafficNodes node) {
        this.node = node;
    }

    public long getAverageBytesPerSec() {
        return averageBytesPerSec;
    }

    public long getAverageMsPerSec() {
        return averageMsPerSec;
    }

    public long getTenSecBytesPerSec() {
        return tenSecBytesPerSec;
    }

    public long getTenSecMsPerSec() {
        return tenSecMsPerSec;
    }

    public TrafficNodes getNode() {
        return node;
    }

}
