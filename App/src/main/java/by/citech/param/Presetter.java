package by.citech.param;

import android.util.Log;

import by.citech.codec.audio.AudioCodecType;

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
                case Normal:
                    Settings.opMode = OpMode.Normal;
                    Settings.btSinglePacket = false;
                    break;
                default:
                    Log.e(TAG, "no matches for opMode, set to default");
                    break;
            }
        } else {
            Log.e(TAG, "opMode is null, set to default");
        }
        if (debug) Log.w(TAG, "opMode set to " + Settings.opMode.getSettingName());
    }

    public static void setAudioCodecType(AudioCodecType audioCodecType) {
        if (audioCodecType == null) {
            Log.e(TAG, "illegal value audioCodecType, set to default");
        } else {
            Settings.audioCodecType = audioCodecType;
        }
        if (debug) Log.w(TAG, "audioCodecType set to " + Settings.audioCodecType.getSettingName());
    }

    public static void setBt2NetFactor(int bt2NetFactor) {
        if (bt2NetFactor < 0) {
            Log.e(TAG, "illegal value bt2NetFactor, set to default");
        } else {
            Settings.bt2NetFactor = bt2NetFactor;
        }
        if (debug) Log.w(TAG, "bt2NetFactor set to " + Settings.bt2NetFactor);
    }

    public static void setBtLatencyMs(int btLatencyMs) {
        if (btLatencyMs < 0) {
            Log.e(TAG, "illegal value btLatencyMs, set to default");
        } else {
            Settings.btLatencyMs = btLatencyMs;
        }
        if (debug) Log.w(TAG, "btLatencyMs set to " + Settings.btLatencyMs);
    }

    public static void setBtSinglePacket(boolean btSinglePacket) {
        Settings.btSinglePacket = btSinglePacket;
        if (debug) Log.w(TAG, "btSinglePacket set to " + Settings.btSinglePacket);
    }

}
