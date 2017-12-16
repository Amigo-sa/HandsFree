package by.citech.handsfree.settings;

public interface ISettingEnum<T extends ISettingEnum> {
    String getSettingName();
    String getSettingNumber();
    String getDefaultName();
    String getTypeName();
    T getDefaultValue();
    T[] getValues();
}
