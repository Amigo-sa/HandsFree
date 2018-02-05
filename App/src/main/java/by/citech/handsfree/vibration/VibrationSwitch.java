package by.citech.handsfree.vibration;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;

import by.citech.handsfree.application.ThisApp;

public class VibrationSwitch {

    private static final int MAX_VIBRATION = 20000;

    private Vibrator vibrator;
    private long[] pattern;
    private int repeat;
    private Runnable stop = () -> vibrator.cancel();
    private Handler handler;

    public VibrationSwitch(Handler handler) {
        pattern = new long[]{400, 600, 400, 600, 400, 600};
        repeat = pattern.length - 2;
        this.handler = handler;
        vibrator = (Vibrator) ThisApp
                .getAppContext()
                .getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void turnOn() {
        if (!isReady()) return;
        vibrator.vibrate(pattern, repeat);
        handler.postDelayed(stop, MAX_VIBRATION);
    }

    public void turnOff() {
        if (!isReady()) return;
        handler.removeCallbacks(stop);
        vibrator.cancel();
    }

    private boolean isReady() {
        return vibrator != null && handler != null;
    }

}
