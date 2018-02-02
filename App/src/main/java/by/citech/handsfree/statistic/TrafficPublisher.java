package by.citech.handsfree.statistic;

import by.citech.handsfree.statistic.TrafficAnalyzer;
import by.citech.handsfree.statistic.TrafficInfo;

public class TrafficPublisher implements TrafficAnalyzer.ITrafficReporter {
    @Override
    public void publishTrafficInfo(TrafficInfo trafficInfo) {}
    @Override
    public void updateTrafficInfo() {}
}
