package by.citech.param;

import android.util.Log;

public interface ISettings {

    String TAG = Tags.I_SETTINGS;
    String MSG_ERR = StatusMessages.ERR_NOT_OVERRIDED;
    String MSG_WRN = StatusMessages.WRN_NOT_OVERRIDED;

    default void initiate() {
        Log.w(TAG, "takeSettings" + MSG_WRN);
        takeSettings();
        applySettings();
    }

    default void takeSettings() {
        Log.e(TAG, "takeSettings" + MSG_ERR);
    }

    default void applySettings() {
        Log.e(TAG, "applySettings" + MSG_ERR);
    }

}
