package by.citech.handsfree.settings;

import by.citech.handsfree.settings.enumeration.ISettingCategory;

public interface ISettingsReset {
    boolean resetSettings();
    boolean resetSetting(ISettingCategory iSettingCategory);
}
