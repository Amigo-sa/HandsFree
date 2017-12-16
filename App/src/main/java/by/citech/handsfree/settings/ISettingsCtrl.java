package by.citech.handsfree.settings;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface ISettingsCtrl {

    String TAG = Tags.I_SETTINGS;
    String ERR_OVR = StatusMessages.ERR_NOT_OVERRIDED;

    default boolean takeSettings() {
        return Settings.subscribe(new SettingsSubscriber(this));
    }

    default boolean applySettings(SeverityLevel severityLevel) {
        Log.e(TAG, "applySettings" + ERR_OVR);
        return false;
    }

}
