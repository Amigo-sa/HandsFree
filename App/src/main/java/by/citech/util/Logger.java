package by.citech.util;

import android.util.Log;

public class Logger {

    private static final String APP_NAME = "HandFree";

    public static void error(String msg) {
        Log.e(APP_NAME, msg);
    }

    public static void debug(String msg) {
        Log.d(APP_NAME, msg);
    }
}
