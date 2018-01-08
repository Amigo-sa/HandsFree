package by.citech.handsfree.settings.category;

public interface ISettingCategory {
    Object getDefaultValue();
    boolean setToDefault();
    boolean setToValue(Object o);
//    String
}
