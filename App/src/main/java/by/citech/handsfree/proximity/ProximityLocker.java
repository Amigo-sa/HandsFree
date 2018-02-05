package by.citech.handsfree.proximity;

import android.os.PowerManager;

import by.citech.handsfree.application.ThisApp;

import static android.content.Context.POWER_SERVICE;

public class ProximityLocker {

    private final static long TIMEOUT = 10*60*1000L;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    //-------------------------- constructor

    public ProximityLocker() {
        powerManager = (PowerManager) ThisApp
                .getAppContext()
                .getSystemService(POWER_SERVICE);
        if (powerManager == null) return;
        wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                this.getClass().getCanonicalName());
    }

    //-------------------------- main

    public void turnOn() {
        if (!isReady()) return;
        if (!wakeLock.isHeld()) wakeLock.acquire(TIMEOUT);
    }

    public void turnOff() {
        if (!isReady()) return;
        if (wakeLock.isHeld()) wakeLock.release();
    }

    //-------------------------- additional

    private boolean isReady() {
        return powerManager != null && wakeLock != null;
    }

}
