package by.citech.handsfree.settings;

import android.support.annotation.CallSuper;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface ISettingsCtrl {

    String TAG = Tags.I_SETTINGS;
    String ERR_OVR = StatusMessages.ERR_NOT_OVERRIDED;

    @CallSuper
    default boolean takeSettings() {
        return Settings.subscribe(new SettingsSubscriber(this));
    }

    @CallSuper
    default boolean applySettings(SeverityLevel severityLevel) {
        return false;
    }

}
