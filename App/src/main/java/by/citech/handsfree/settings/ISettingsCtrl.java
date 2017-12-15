package by.citech.handsfree.settings;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface ISettingsCtrl {

    String TAG = Tags.I_SETTINGS;
    String MSG_ERR = StatusMessages.ERR_NOT_OVERRIDED;
    String MSG_WRN = StatusMessages.WRN_NOT_OVERRIDED;

    default void initSettings() {
        Log.w(TAG, "initSettings" + MSG_WRN);
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
