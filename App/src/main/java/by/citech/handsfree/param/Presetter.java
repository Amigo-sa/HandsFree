package by.citech.handsfree.param;

import android.util.Log;

import by.citech.handsfree.codec.audio.AudioCodecType;

public class Presetter {

    private static final String TAG = Tags.PRESETTER;
    private static final boolean debug = Settings.debug;

    public static void setOpMode(OpMode opMode) {
        if (opMode != null) {
            switch (opMode) {
                case Bt2Bt:
                    Settings.opMode = OpMode.Bt2Bt;
                    Settings.btSinglePacket = false;
                    Settings.btFactor = Settings.bt2NetFactor;
                    break;
                case AudIn2Bt:
                    Settings.opMode = OpMode.AudIn2Bt;
                    Settings.btSinglePacket = true;
                    Settings.audioSingleFrame = true;
                    Settings.audioBuffIsShorts = true;
                    break;
                case Bt2AudOut:
                    Settings.opMode = OpMode.Bt2AudOut;
                    Settings.btSinglePacket = true;
                    Settings.audioSingleFrame = true;
                    Settings.audioBuffIsShorts = true;
                    break;
                case AudIn2AudOut:
                    Settings.opMode = OpMode.AudIn2AudOut;
                    Settings.audioSingleFrame = false;
                    Settings.audioBuffSizeBytes = 24000;
                    Settings.audioBuffIsShorts = true;
                    break;
                case Record:
                    Settings.opMode = OpMode.Record;
                    Settings.btSinglePacket = false;
                    Settings.btFactor = Settings.bt2NetFactor;
                    break;
                case Net2Net:
                    setToDefault();
                    Log.e(TAG, "setOpMode opMode is Net2Net (not implemented), set to default");
                    break;
                case Normal:
                    setToDefault();
                    break;
                default:
                    setToDefault();
                    Log.e(TAG, "setOpMode no matches for opMode, set to default");
                    break;
            }
        } else {
            Settings.opMode = SettingsDefault.Common.opMode;
            Log.e(TAG, "setOpMode illegal value, set to default");
        }
        if (debug) Log.w(TAG, "setOpMode opMode set to " + Settings.opMode.getSettingName());
    }

    private static void setToDefault() {
        Settings.opMode = SettingsDefault.Common.opMode;
        Settings.btSinglePacket = SettingsDefault.Bluetooth.btSinglePacket;
        Settings.btFactor = SettingsDefault.Bluetooth.btFactor;
    }

    public static void setAudioCodecType(AudioCodecType audioCodecType) {
        if (audioCodecType == null) {
            Settings.audioCodecType = SettingsDefault.AudioCommon.audioCodecType;
            Log.e(TAG, "setAudioCodecType illegal value, set to default");
        } else {
            Settings.audioCodecType = audioCodecType;
        }
        if (debug) Log.w(TAG, "audioCodecType set to " + Settings.audioCodecType.getSettingName());
    }

    public static void setBt2NetFactor(int bt2NetFactor) {
        if (bt2NetFactor < 0) {
            Settings.bt2NetFactor = SettingsDefault.Bluetooth.bt2NetFactor;
            Log.e(TAG, "setBt2NetFactor illegal value, set to default");
        } else {
            Settings.bt2NetFactor = bt2NetFactor;
        }
        if (debug) Log.w(TAG, "setBt2NetFactor set to " + Settings.bt2NetFactor);
    }

    public static void setBtLatencyMs(int btLatencyMs) {
        if (btLatencyMs < 0) {
            Settings.btLatencyMs = SettingsDefault.Bluetooth.btLatencyMs;
            Log.e(TAG, "setBtLatencyMs illegal value, set to default");
        } else {
            Settings.btLatencyMs = btLatencyMs;
        }
        if (debug) Log.w(TAG, "setBtLatencyMs set to " + Settings.btLatencyMs);
    }

    public static void setBtSinglePacket(boolean btSinglePacket) {
        Settings.btSinglePacket = btSinglePacket;
        if (debug) Log.w(TAG, "setBtSinglePacket set to " + Settings.btSinglePacket);
    }

}
