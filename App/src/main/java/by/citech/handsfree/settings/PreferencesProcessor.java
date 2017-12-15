package by.citech.handsfree.settings;

import android.content.Context;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManagerFix;
import android.util.Log;

import by.citech.handsfree.R;
import by.citech.handsfree.param.Tags;

public class PreferencesProcessor {

    private static final String TAG = Tags.PREF_PROCESSOR;
    private static final boolean debug = Settings.debug;

    public static void process(Context context) {
        PreferenceManagerFix.setDefaultValues(context, R.xml.settings, false);
        SharedPreferences prefs = PreferenceManagerFix.getDefaultSharedPreferences(context);
        processAudioCodecType(prefs);
        processBtSinglePacket(prefs);
        processBt2NetFactor(prefs);
        processBtLatencyMs(prefs);
        processOpMode(prefs);
    }

    private static void processAudioCodecType(SharedPreferences prefs) {
        Presetter.setAudioCodecType(processEnum(prefs, SettingsDefault.AudioCommon.audioCodecType));
    }

    private static void processOpMode(SharedPreferences prefs) {
        Presetter.setOpMode(processEnum(prefs, SettingsDefault.Common.opMode));
    }

    private static <T extends ISettingEnum<T>> T processEnum(SharedPreferences prefs, T defaultT) {
        String read = prefs.getString(defaultT.getSettingKey(), defaultT.getDefaultSettingName());
        if (read == null || read.isEmpty()) {
            Log.e(TAG, "processEnum read illegal value" + read);
        } else {
            if (debug) Log.i(TAG, "processEnum read is " + read);
            for (T t : defaultT.getValues()) {
                if (read.matches(t.getSettingNumber())) {
                    if (debug) Log.i(TAG, "processEnum found matching setting: " + t.getSettingName());
                    return t;
                }
            }
        }
        return defaultT;
    }

    private static void processBtLatencyMs(SharedPreferences prefs) {
        Presetter.setBtLatencyMs(Integer.parseInt(
                prefs.getString(SettingsDefault.Key.btLatencyMs,
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs))));
    }

    private static void processBt2NetFactor(SharedPreferences prefs) {
        Presetter.setBt2NetFactor(Integer.parseInt(
                prefs.getString(SettingsDefault.Key.bt2NetFactor,
                Integer.toString(SettingsDefault.Bluetooth.bt2NetFactor))));
    }

    private static void processBtSinglePacket(SharedPreferences prefs) {
        Presetter.setBtSinglePacket(
                prefs.getBoolean(SettingsDefault.Key.btSinglePacket,
                SettingsDefault.Bluetooth.btSinglePacket));
    }

}
