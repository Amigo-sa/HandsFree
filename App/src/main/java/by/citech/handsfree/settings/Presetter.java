package by.citech.handsfree.settings;

import android.util.Log;

import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.parameters.Tags;

public class Presetter {

    private static final String STAG = Tags.Presetter + " ST";
    private static final boolean debug = Settings.debug;

    static void setOpMode(EOpMode opMode) {
        if (opMode != null) {
            switch (opMode) {
                case Bt2Bt:
                    Settings.opMode = EOpMode.Bt2Bt;
                    Settings.btSinglePacket = false;
                    Settings.btFactor = Settings.bt2NetFactor;
                    break;
                case DataGen2Bt:
                    Settings.opMode = EOpMode.DataGen2Bt;
                    Settings.btSinglePacket = true;
                    Settings.audioSingleFrame = true;
                    Settings.audioBuffIsShorts = true;
                    break;
                case AudIn2Bt:
                    Settings.opMode = EOpMode.AudIn2Bt;
                    Settings.btSinglePacket = true;
                    Settings.audioSingleFrame = true;
                    Settings.audioBuffIsShorts = true;
                    break;
                case Bt2AudOut:
                    Settings.opMode = EOpMode.Bt2AudOut;
                    Settings.audioSingleFrame = false;
                    Settings.audioBuffSizeBytes = 24000;
                    Settings.audioBuffIsShorts = true;
                    break;
                case AudIn2AudOut:
                    Settings.opMode = EOpMode.AudIn2AudOut;
                    Settings.audioSingleFrame = false;
                    Settings.audioBuffSizeBytes = 24000;
                    Settings.audioBuffIsShorts = true;
                    break;
                case Record:
                    Settings.opMode = EOpMode.Record;
                    Settings.btSinglePacket = false;
                    Settings.btFactor = Settings.bt2NetFactor;
                    break;
                case Net2Net:
                    setToDefault();
                    if (debug) Log.e(STAG, "setOpMode opMode is Net2Net (not implemented), set to default");
                    break;
                case Normal:
                    setToDefault();
                    break;
                default:
                    setToDefault();
                    if (debug) Log.e(STAG, "setOpMode no matches for opMode, set to default");
                    break;
            }
        } else {
            Settings.opMode = SettingsDefault.Common.opMode;
            if (debug) Log.e(STAG, "setOpMode illegal value, set to default");
        }
        if (debug) Log.w(STAG, "setOpMode opMode set to " + Settings.opMode.getSettingName());
    }

    private static void setToDefault() {
        Settings.opMode = SettingsDefault.Common.opMode;
        Settings.btSinglePacket = SettingsDefault.Bluetooth.btSinglePacket;
        Settings.btFactor = SettingsDefault.Bluetooth.btFactor;
        Settings.bt2BtPacketSize = SettingsDefault.Bluetooth.bt2BtPacketSize;
    }

    static void setAudioCodecType(EAudioCodecType audioCodecType) {
        if (audioCodecType == null) {
            Settings.audioCodecType = SettingsDefault.AudioCommon.audioCodecType;
            if (debug) Log.e(STAG, "setAudioCodecType illegal value, set to default");
        } else {
            Settings.audioCodecType = audioCodecType;
        }
        if (debug) Log.w(STAG, "audioCodecType set to " + Settings.audioCodecType.getSettingName());
    }

    static void setBt2NetFactor(int bt2NetFactor) {
        if (bt2NetFactor < 0) {
            Settings.bt2NetFactor = SettingsDefault.Common.bt2NetFactor;
            if (debug) Log.e(STAG, "setBt2NetFactor illegal value, set to default");
        } else {
            Settings.bt2NetFactor = bt2NetFactor;
        }
        if (debug) Log.w(STAG, "setBt2NetFactor set to " + Settings.bt2NetFactor);
    }

    static void setBt2BtPacketSize(int bt2BtPacketSize) {
        if (bt2BtPacketSize < 0) {
            Settings.bt2BtPacketSize = SettingsDefault.Bluetooth.bt2BtPacketSize;
            if (debug) Log.e(STAG, "setBt2BtPacketSize illegal value, set to default");
        } else {
            Settings.bt2BtPacketSize = bt2BtPacketSize;
        }
        if (debug) Log.w(STAG, "setBt2BtPacketSize set to " + Settings.bt2BtPacketSize);
    }

    static void setBtLatencyMs(int btLatencyMs) {
        if (btLatencyMs < 0) {
            Settings.btLatencyMs = SettingsDefault.Bluetooth.btLatencyMs;
            if (debug) Log.e(STAG, "setBtLatencyMs illegal value, set to default");
        } else {
            Settings.btLatencyMs = btLatencyMs;
        }
        if (debug) Log.w(STAG, "setBtLatencyMs set to " + Settings.btLatencyMs);
    }

    static void setBtSinglePacket(boolean btSinglePacket) {
        Settings.btSinglePacket = btSinglePacket;
        if (debug) Log.w(STAG, "setBtSinglePacket set to " + Settings.btSinglePacket);
    }

}
