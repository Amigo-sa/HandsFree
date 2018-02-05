package by.citech.handsfree.settings;

import android.content.Context;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManagerFix;

import by.citech.handsfree.R;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import timber.log.Timber;

public class PreferencesProcessor {

    private static final boolean debug = Settings.debug;

    private static SharedPreferences prefs;

    //-------------------------- save/restore/reset

    public static void init(Context context) {
//      ApplicationInfo applicationInfo = context.getApplicationInfo();
//      int stringId = applicationInfo.labelRes;
//      preferences = context.getSharedPreferences(context.getString(stringId), MODE_PRIVATE);
        prefs = PreferenceManagerFix.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }

    public static SharedPreferences.Editor getEditor() {
        return prefs.edit();
    }

    //-------------------------- applyPrefsToSettings

    public static void applyPrefsToSettings(Context context) {
        Timber.i("applyPrefsToSettings");
        PreferenceManagerFix.setDefaultValues(context, R.xml.settings, false);
        Presetter.setAudioCodecType(processEnum(SettingsDefault.AudioCommon.audioCodecType));
        Presetter.setBtSinglePacket(getBtSinglePacketPref());
        Presetter.setBt2BtPacketSize(getBt2btPacketSizePref());
        Presetter.setBt2NetFactor(getBt2NetFactorPref());
        Presetter.setBtLatencyMs(getBtLatencyMsPref());
        Presetter.setBtChosenAddr(getBtChosenAddrPref());
        Presetter.setOpMode(processEnum(SettingsDefault.Common.opMode));
    }

    //-------------------------- saving preferences

    public static void saveBtChosenAddrPref(String newValue) {
        savePref(SettingsDefault.TypeName.btChosenAddr, newValue);
        Presetter.setBtChosenAddr(newValue);
    }

    public static void saveBtLatencyMsPref(int newValue) {
        savePref(SettingsDefault.TypeName.btLatencyMs, newValue);
        Presetter.setBtLatencyMs(newValue);
    }

    public static void saveBt2btPacketSizePref(int newValue) {
        savePref(SettingsDefault.TypeName.bt2BtPacketSize, newValue);
        Presetter.setBt2BtPacketSize(newValue);
    }

    public static void saveBt2NetFactorPref(int newValue) {
        savePref(SettingsDefault.TypeName.bt2NetFactor, newValue);
        Presetter.setBt2NetFactor(newValue);
    }

    public static void saveBtSinglePacketPref(boolean newValue) {
        savePref(SettingsDefault.TypeName.btSinglePacket, newValue);
        Presetter.setBtSinglePacket(newValue);
    }

    public static void saveAudioCodecTypePref(EAudioCodecType newValue) {
        savePref(SettingsDefault.TypeName.audioCodecType, newValue.getTypeName());
        Presetter.setAudioCodecType(newValue);
    }

    public static void saveOpModePref(EOpMode newValue) {
        savePref(SettingsDefault.TypeName.opMode, newValue.getTypeName());
        Presetter.setOpMode(newValue);
    }

    //-------------------------- common saving

    private static void savePref(String typeName, int newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(typeName, newValue);
        editor.apply();
    }

    private static void savePref(String typeName, String newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(typeName, newValue);
        editor.apply();
    }

    private static void savePref(String typeName, boolean newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(typeName, newValue);
        editor.apply();
    }

    //-------------------------- getting preferences

    public static EOpMode getOpModePref() {
        return processEnum(SettingsDefault.Common.opMode);
    }

    public static EAudioCodecType getAudioCodecTypePref() {
        return processEnum(SettingsDefault.AudioCommon.audioCodecType);
    }

    public static String getBtChosenAddrPref() {
        return prefs.getString(
                SettingsDefault.TypeName.btChosenAddr,
                SettingsDefault.Bluetooth.btChosenAddr);
    }

    public static int getBtLatencyMsPref() {
        return Integer.parseInt(prefs.getString(
                SettingsDefault.TypeName.btLatencyMs,
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs)));
    }

    public static int getBt2btPacketSizePref() {
        return Integer.parseInt(prefs.getString(
                SettingsDefault.TypeName.bt2BtPacketSize,
                Integer.toString(SettingsDefault.Bluetooth.bt2BtPacketSize)));
    }

    public static int getBt2NetFactorPref() {
        return Integer.parseInt(prefs.getString(
                SettingsDefault.TypeName.bt2NetFactor,
                Integer.toString(SettingsDefault.Common.bt2NetFactor)));
    }

    public static boolean getBtSinglePacketPref() {
        return prefs.getBoolean(
                SettingsDefault.TypeName.btSinglePacket,
                SettingsDefault.Bluetooth.btSinglePacket);
    }

    //-------------------------- common

    private static <T extends ISettingEnum<T>> String getEnumPref(T defaultT) {
        return prefs.getString(defaultT.getTypeName(), defaultT.getDefaultName());
    }

    private static <T extends ISettingEnum<T>> T processEnum(T defaultT) {
        String read = getEnumPref(defaultT);
        if (read == null || read.isEmpty()) {
            Timber.e("processEnum read illegal value: <%s>", read);
        } else {
            Timber.i("processEnum read: <%s>", read);
            for (T t : defaultT.getValues()) {
                if (read.matches(t.getSettingNumber())) {
                    Timber.i("processEnum found matching setting: <%s>", t);
                    return t;
                }
            }
        }
        return defaultT;
    }

}
