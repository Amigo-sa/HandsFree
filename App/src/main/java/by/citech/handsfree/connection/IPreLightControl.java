package by.citech.handsfree.connection;

import android.support.annotation.CallSuper;

public interface IPreLightControl {

    @CallSuper
    default void tryToToggleLight(boolean isOn, LightConfig.EArea area, LightConfig.ELight light) {
        LightControl.getInstance().tryToToggleLight(isOn, area, light);
    }

}
