package by.citech.handsfree.settings;

import by.citech.handsfree.settings.category.ISettingCategory;

public interface ISettingsReset {
    boolean resetSettings();
    boolean resetSetting(ISettingCategory iSettingCategory);
}
