package by.citech.handsfree.settings;

import android.support.annotation.CallSuper;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface ISettingsCtrl {

    String TAG = Tags.ISettingsCtrl;
    String ERR_OVR = StatusMessages.ERR_NOT_OVERRIDED;

    @CallSuper
    default boolean takeSettings() {
//        return Settings.subscribe(new SettingsSubscriber(this));
        return true;
    }

    @CallSuper
    default boolean applySettings(ESeverityLevel severityLevel) {
        return false;
    }

}
