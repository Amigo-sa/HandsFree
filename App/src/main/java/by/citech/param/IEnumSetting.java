package by.citech.param;

public interface IEnumSetting<T extends Enum<T> & IEnumSetting> {
    String getSettingName();
    String getSettingNumber();
    String getDefaultSettingName();
    String getSettingKey();
    T getDefaultSetting();
}
