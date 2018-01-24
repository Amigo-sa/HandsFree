package by.citech.handsfree.settings;

import android.content.Context;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManagerFix;
import android.util.Log;

import by.citech.handsfree.R;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

import static by.citech.handsfree.settings.Presetter.setBtLatencyMs;

public class PreferencesProcessor {

    private static final String STAG = Tags.PreferencesProcessor + " ST";
    private static final boolean debug = Settings.debug;

    private static SharedPreferences prefs;

    //-------------------------- save/restore/reset

    public static void init(Context context) {
//      ApplicationInfo applicationInfo = context.getApplicationInfo();
//      int stringId = applicationInfo.labelRes;
//      preferences = context.getSharedPreferences(context.getString(stringId), MODE_PRIVATE);
        prefs = PreferenceManagerFix.getDefaultSharedPreferences(context);
    }

    public static String getBtChosenAddr() {
        return Settings.Bluetooth.btChosendAddr;
    }

    public static void saveBtChosenAddr(String btAddrToChoose) {
        Settings.Bluetooth.btChosendAddr = btAddrToChoose;
        saveBtChosenAddrPref(Settings.Bluetooth.btChosendAddr);
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }

    public static SharedPreferences.Editor getEditor() {
        return prefs.edit();
    }

    //-------------------------- saving preferences

    public static void saveBtChosenAddrPref(String newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(SettingsDefault.TypeName.btChosenAddr, newValue);
        editor.apply();
        Presetter.setBtChosenAddr(newValue);
    }

    public static void saveBtLatencyMsPref(int newValue) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(SettingsDefault.TypeName.btLatencyMs, newValue);
        editor.apply();
        setBtLatencyMs(newValue);
    }

    //-------------------------- getting preferences

    public static String getBtChosenAddrPref() {
        return prefs.getString(
                SettingsDefault.TypeName.btChosenAddr,
                SettingsDefault.Bluetooth.btChosenAddr);
    }

    public static String getBtLatencyMsPref() {
        return prefs.getString(
                SettingsDefault.TypeName.btLatencyMs,
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs));
    }

    //-------------------------- process

    public static void process(Context context) {
        if (debug) Log.i(STAG, "process");
        PreferenceManagerFix.setDefaultValues(context, R.xml.settings, false);
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(context);
        processAudioCodecType(prefs);
        processBtSinglePacket(prefs);
        processBt2btPacketSize(prefs);
        processBt2NetFactor(prefs);
        processBtLatencyMs(prefs);
        processOpMode(prefs);
    }

    private static <T extends ISettingEnum<T>> T processEnum(SharedPreferences prefs, T defaultT) {
        if (prefs == null || defaultT == null) {
            if (debug) Log.e(STAG, "processEnum" + StatusMessages.ERR_PARAMETERS);
            return null;
        }
        String read = prefs.getString(defaultT.getTypeName(), defaultT.getDefaultName());
        if (read == null || read.isEmpty()) {
            if (debug) Log.e(STAG, "processEnum read illegal value" + read);
        } else {
            if (debug) Log.i(STAG, "processEnum read is " + read);
            for (T t : defaultT.getValues()) {
                if (read.matches(t.getSettingNumber())) {
                    if (debug) Log.i(STAG, "processEnum found matching setting: " + t.getSettingName());
                    return t;
                }
            }
        }
        return defaultT;
    }

    private static void processAudioCodecType(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processAudioCodecType" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        Presetter.setAudioCodecType(processEnum(prefs, SettingsDefault.AudioCommon.audioCodecType));
    }

    private static void processOpMode(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processOpMode" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        Presetter.setOpMode(processEnum(prefs, SettingsDefault.Common.opMode));
    }

    private static void processBtLatencyMs(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processBtLatencyMs" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        setBtLatencyMs(Integer.parseInt(
                prefs.getString(SettingsDefault.TypeName.btLatencyMs,
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs))));
    }


    private static void processBt2btPacketSize(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processBt2btPacketSize" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        Presetter.setBt2BtPacketSize(Integer.parseInt(
                prefs.getString(SettingsDefault.TypeName.bt2BtPacketSize,
                Integer.toString(SettingsDefault.Bluetooth.bt2BtPacketSize))));
    }

    private static void processBt2NetFactor(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processBt2NetFactor" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        Presetter.setBt2NetFactor(Integer.parseInt(
                prefs.getString(SettingsDefault.TypeName.bt2NetFactor,
                Integer.toString(SettingsDefault.Common.bt2NetFactor))));
    }

    private static void processBtSinglePacket(SharedPreferences prefs) {
        if (prefs == null) {
            if (debug) Log.e(STAG, "processBtSinglePacket" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        Presetter.setBtSinglePacket(
                prefs.getBoolean(SettingsDefault.TypeName.btSinglePacket,
                SettingsDefault.Bluetooth.btSinglePacket));
    }

}
