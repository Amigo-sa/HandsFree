package by.citech.handsfree.ui.helpers;

import by.citech.handsfree.statistic.TrafficAnalyzer;
import by.citech.handsfree.statistic.TrafficInfo;

public class TrafficHelper implements TrafficAnalyzer.ITrafficReporter {
    @Override
    public void publishTrafficInfo(TrafficInfo trafficInfo) {}
    @Override
    public void updateTrafficInfo() {}
}
