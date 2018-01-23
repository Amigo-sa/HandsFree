package by.citech.handsfree.traffic;

import android.support.annotation.CallSuper;

import java.util.Arrays;

import by.citech.handsfree.settings.Settings;

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
