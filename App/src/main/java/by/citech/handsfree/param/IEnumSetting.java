package by.citech.handsfree.param;

public interface IEnumSetting<T extends IEnumSetting> {
    String getSettingName();
    String getSettingNumber();
    String getDefaultSettingName();
    String getSettingKey();
    T getDefaultSetting();
    T[] getValues();
}
