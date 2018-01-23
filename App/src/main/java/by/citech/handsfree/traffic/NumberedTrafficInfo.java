package by.citech.handsfree.traffic;

public class NumberedTrafficInfo {

    private int    packetSize;
    private long   lastLostPacketsAmount;
    private long   maxLostPacketsAmount;
    private long   totalPacketsCount;
    private long   totalReceivedPacketsCount;
    private long   totalLostPacketsCount;
    private double totalBytesPerSec;
    private long   totalBytesCount;
    private double totalLostPercent;
    private long   deltaLostPacketsCount;
    private double deltaLostPercent;
    private double deltaBytesPerSec;

    void update(
            int    packetSize,
            long   lastLostPacketsAmount,
            long   maxLostPacketsAmount,
            long   totalPacketsCount,
            long   totalReceivedPacketsCount,
            long   totalLostPacketsCount,
            double totalBytesPerSec,
            long   totalBytesCount,
            double totalLostPercent,
            long   deltaLostPacketsCount,
            double deltaLostPercent,
            double deltaBytesPerSec
    ) {
        this.packetSize                = packetSize               ;
        this.lastLostPacketsAmount     = lastLostPacketsAmount    ;
        this.maxLostPacketsAmount      = maxLostPacketsAmount     ;
        this.totalPacketsCount         = totalPacketsCount        ;
        this.totalReceivedPacketsCount = totalReceivedPacketsCount;
        this.totalLostPacketsCount     = totalLostPacketsCount    ;
        this.totalBytesPerSec          = totalBytesPerSec         ;
        this.totalBytesCount           = totalBytesCount          ;
        this.totalLostPercent          = totalLostPercent         ;
        this.deltaLostPacketsCount     = deltaLostPacketsCount    ;
        this.deltaLostPercent          = deltaLostPercent         ;
        this.deltaBytesPerSec          = deltaBytesPerSec         ;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public long getLastLostPacketsAmount() {
        return lastLostPacketsAmount;
    }

    public long getMaxLostPacketsAmount() {
        return maxLostPacketsAmount;
    }

    public long getTotalPacketsCount() {
        return totalPacketsCount;
    }

    public long getTotalReceivedPacketsCount() {
        return totalReceivedPacketsCount;
    }

    public long getTotalLostPacketsCount() {
        return totalLostPacketsCount;
    }

    public double getTotalBytesPerSec() {
        return totalBytesPerSec;
    }

    public long getTotalBytesCount() {
        return totalBytesCount;
    }

    public double getTotalLostPercent() {
        return totalLostPercent;
    }

    public long getDeltaLostPacketsCount() {
        return deltaLostPacketsCount;
    }

    public double getDeltaLostPercent() {
        return deltaLostPercent;
    }

    public double getDeltaBytesPerSec() {
        return deltaBytesPerSec;
    }
}
