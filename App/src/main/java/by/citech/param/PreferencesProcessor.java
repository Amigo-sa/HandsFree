package by.citech.param;

import android.content.Context;
import android.content.SharedPreferences;

import android.support.v7.preference.PreferenceManagerFix;
import android.util.Log;

import by.citech.R;
import by.citech.codec.audio.AudioCodecType;

public class PreferencesProcessor {

    private static final String TAG = Tags.PREF_PROCESSOR;
    private static final boolean debug = Settings.debug;

    private Context context;
    private SharedPreferences prefs;

    public PreferencesProcessor(Context context) {
        this.context = context;
    }

    public void processPreferences() {
        PreferenceManagerFix.setDefaultValues(context, R.xml.settings, false);
        prefs = PreferenceManagerFix.getDefaultSharedPreferences(context);
        processAudioCodecType();
        processBtSinglePacket();
        processBt2NetFactor();
        processBtLatencyMs();
        processOpMode();
    }

    private void processAudioCodecType() {
        Presetter.setAudioCodecType(processEnum(
                AudioCodecType.class,
                SettingsDefault.AudioCommon.audioCodecType));
    }

    private void processOpMode() {
        Presetter.setOpMode(processEnum(
                OpMode.class,
                SettingsDefault.Common.opMode));
    }

    private <T extends Enum<T> & IEnumSetting> T processEnum(Class<T> clazz, T defaultT) {
        String read = prefs.getString(defaultT.getSettingKey(), defaultT.getDefaultSettingName());
        if (read == null || read.isEmpty()) {
            Log.e(TAG, "processEnum read illegal value" + read);
        } else {
            if (debug) Log.i(TAG, "processEnum read is " + read);
            for (T t : clazz.getEnumConstants()) {
                if (read.matches(t.getSettingNumber())) {
                    if (debug) Log.i(TAG, "processEnum found matching setting: " + t.getSettingName());
                    return t;
                }
            }
        }
        return defaultT;
    }

    private void processBtLatencyMs() {
        Presetter.setBtLatencyMs(Integer.parseInt(
                prefs.getString(SettingsDefault.Key.btLatencyMs,
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs))));
    }

    private void processBt2NetFactor() {
        Presetter.setBt2NetFactor(Integer.parseInt(
                prefs.getString(SettingsDefault.Key.bt2NetFactor,
                Integer.toString(SettingsDefault.Bluetooth.bt2NetFactor))));
    }

    private void processBtSinglePacket() {
        Presetter.setBtSinglePacket(
                prefs.getBoolean(SettingsDefault.Key.btSinglePacket,
                SettingsDefault.Bluetooth.btSinglePacket));
    }

}
