package by.citech.handsfree.settings;

public interface ISettingEnum<T extends ISettingEnum> {
    String getSettingName();
    String getSettingNumber();
    String getDefaultSettingName();
    String getSettingKey();
    T getDefaultSetting();
    T[] getValues();
}
