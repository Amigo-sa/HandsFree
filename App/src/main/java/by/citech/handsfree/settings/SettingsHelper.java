package by.citech.handsfree.settings;

import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;

public class SettingsHelper {

    public static <T extends ISettingEnum<T>> CharSequence[] getCharSequenceArrOfNames(T someT) {
        T[] values = someT.getValues();
        CharSequence[] arr = new CharSequence[values.length];
        for (int i = 0; i < arr.length; i++) {arr[i] = values[i].getSettingName();}
        return arr;
    }

    public static <T extends ISettingEnum<T>> CharSequence[] getCharSequenceArrOfNumbers(T someT) {
        T[] values = someT.getValues();
        CharSequence[] arr = new CharSequence[values.length];
        for (int i = 0; i < arr.length; i++) {arr[i] = values[i].getSettingNumber();}
        return arr;
    }

    public static <T extends ISettingEnum<T>> void prepareListPref(ListPreference pref, T value) {
        if (pref == null) return;
        pref.setKey(value.getTypeName());
        pref.setEntries(SettingsHelper.getCharSequenceArrOfNames(value));
        pref.setEntryValues(SettingsHelper.getCharSequenceArrOfNumbers(value));
        pref.setValue(value.getSettingNumber());
        pref.setSummary(value.getSettingName());
    }

    public static void prepareEditTextPref(EditTextPreference pref, int value, String key) {
        if (pref == null) return;
        String valueString = String.valueOf(value);
        prepareEditTextPref(pref, valueString, key);
    }

    public static void prepareEditTextPref(EditTextPreference pref, String value, String key) {
        if (pref == null) return;
        pref.setKey(key);
        pref.setText(value);
        pref.setSummary(value);
    }

}
