package by.citech.param;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        processAudioCodecType();
        processBtSinglePacket();
        processBt2NetFactor();
        processBtLatencyMs();
        processOpMode();
    }

    private void processOpMode() {
        OpMode opMode = null;
        String chosenOpMode = prefs.getString(context.getString(R.string.opMode),
                SettingsDefault.Common.opMode.getSettingName());
        for (OpMode mode : OpMode.values()) {
            if (chosenOpMode.matches(mode.getSettingName())) {
                if (debug) Log.i(TAG, "found matching opMode: " + mode.getSettingName());
                opMode = mode;
                Presetter.setOpMode(opMode);
                break;
            }
        }
        if (opMode == null) {
            Presetter.setOpMode(null);
        }
    }

    private void processAudioCodecType() {
        AudioCodecType audioCodecType = null;
        String chosenAudioCodecType = prefs.getString(context.getString(R.string.audioCodecType),
                SettingsDefault.AudioCommon.audioCodecType.getSettingName());
        for (AudioCodecType type : AudioCodecType.values()) {
            if (chosenAudioCodecType.matches(type.getSettingName())) {
                if (debug) Log.i(TAG, "found matching audioCodecType: " + type.getSettingName());
                audioCodecType = type;
                Presetter.setAudioCodecType(audioCodecType);
                break;
            }
        }
        if (audioCodecType == null) {
            Presetter.setAudioCodecType(null);
        }
    }

    private void processBtLatencyMs() {
        Presetter.setBtLatencyMs(Integer.parseInt(
                prefs.getString(context.getString(R.string.btLatencyMs),
                Integer.toString(SettingsDefault.Bluetooth.btLatencyMs))));
    }

    private void processBt2NetFactor() {
        Presetter.setBt2NetFactor(Integer.parseInt(
                prefs.getString(context.getString(R.string.bt2NetFactor),
                Integer.toString(SettingsDefault.Bluetooth.bt2NetFactor))));
    }

    private void processBtSinglePacket() {
        Presetter.setBtSinglePacket(
                prefs.getBoolean(context.getString(R.string.btSinglePacket),
                SettingsDefault.Bluetooth.btSinglePacket));
    }

}
