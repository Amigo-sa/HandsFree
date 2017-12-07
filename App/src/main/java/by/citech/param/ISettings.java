package by.citech.param;

public interface ISettings<T extends Enum<T> & ISettings> {
    String getSettingName();
    String getSettingNumber();
    String getDefaultSettingName();
    String getSettingTypeName();
    T getDefaultSetting();
}
