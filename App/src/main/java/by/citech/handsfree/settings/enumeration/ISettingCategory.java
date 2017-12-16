package by.citech.handsfree.settings.enumeration;

public interface ISettingCategory {
    Object getDefaultValue();
    boolean setToDefault();
    boolean setToValue(Object o);
//    String
}
