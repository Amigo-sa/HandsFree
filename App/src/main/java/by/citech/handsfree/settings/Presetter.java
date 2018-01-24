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
                    Settings.Common.opMode = EOpMode.Bt2Bt;
                    Settings.Bluetooth.btSinglePacket = false;
                    Settings.Bluetooth.btFactor = Settings.Bluetooth.bt2NetFactor;
                    break;
                case DataGen2Bt:
                    Settings.Common.opMode = EOpMode.DataGen2Bt;
                    Settings.Bluetooth.btSinglePacket = true;
                    Settings.AudioCommon.audioSingleFrame = true;
                    Settings.AudioCommon.audioBuffIsShorts = true;
                    break;
                case AudIn2Bt:
                    Settings.Common.opMode = EOpMode.AudIn2Bt;
                    Settings.Bluetooth.btSinglePacket = true;
                    Settings.AudioCommon.audioSingleFrame = true;
                    Settings.AudioCommon.audioBuffIsShorts = true;
                    break;
                case Bt2AudOut:
                    Settings.Common.opMode = EOpMode.Bt2AudOut;
                    Settings.AudioCommon.audioSingleFrame = false;
                    Settings.AudioCommon.audioBuffSizeBytes = 24000;
                    Settings.AudioCommon.audioBuffIsShorts = true;
                    break;
                case AudIn2AudOut:
                    Settings.Common.opMode = EOpMode.AudIn2AudOut;
                    Settings.AudioCommon.audioSingleFrame = false;
                    Settings.AudioCommon.audioBuffSizeBytes = 24000;
                    Settings.AudioCommon.audioBuffIsShorts = true;
                    break;
                case Record:
                    Settings.Common.opMode = EOpMode.Record;
                    Settings.Bluetooth.btSinglePacket = false;
                    Settings.Bluetooth.btFactor = Settings.Bluetooth.bt2NetFactor;
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
            Settings.Common.opMode = SettingsDefault.Common.opMode;
            if (debug) Log.e(STAG, "setOpMode illegal value, set to default");
        }
        if (debug) Log.w(STAG, "setOpMode opMode set to " + Settings.Common.opMode.getSettingName());
    }

    private static void setToDefault() {
        Settings.Common.opMode = SettingsDefault.Common.opMode;
        Settings.Bluetooth.btSinglePacket = false;
        Settings.Bluetooth.btSignificantAll = true;
        Settings.Bluetooth.btSignificantBytes = Settings.Bluetooth.bt2BtPacketSize;
        Settings.netChunkSignificantBytes = Settings.Bluetooth.btSignificantBytes;
        Settings.netChunkSize = Settings.Bluetooth.bt2BtPacketSize;
        Settings.netSignificantAll = Settings.Bluetooth.btSignificantAll;
        Settings.netFactor = Settings.Bluetooth.btFactor;
        Settings.Bluetooth.btSendSize =  Settings.Bluetooth.btSignificantBytes * Settings.Bluetooth.btFactor;
    }

    static void setAudioCodecType(EAudioCodecType audioCodecType) {
        if (audioCodecType == null) {
            Settings.AudioCommon.audioCodecType = SettingsDefault.AudioCommon.audioCodecType;
            if (debug) Log.e(STAG, "setAudioCodecType illegal value, set to default");
        } else {
            Settings.AudioCommon.audioCodecType = audioCodecType;
        }
        if (debug) Log.w(STAG, "audioCodecType set to " + Settings.AudioCommon.audioCodecType.getSettingName());
    }

    static void setBt2NetFactor(int bt2NetFactor) {
        if (bt2NetFactor < 0) {
            Settings.Bluetooth.bt2NetFactor = SettingsDefault.Common.bt2NetFactor;
            if (debug) Log.e(STAG, "setBt2NetFactor illegal value, set to default");
        } else {
            Settings.Bluetooth.bt2NetFactor = bt2NetFactor;
        }
        if (debug) Log.w(STAG, "setBt2NetFactor set to " + Settings.Bluetooth.bt2NetFactor);
    }

    static void setBt2BtPacketSize(int bt2BtPacketSize) {
        if (bt2BtPacketSize < 0) {
            Settings.Bluetooth.bt2BtPacketSize = SettingsDefault.Bluetooth.bt2BtPacketSize;
            if (debug) Log.e(STAG, "setBt2BtPacketSize illegal value, set to default");
        } else {
            Settings.Bluetooth.bt2BtPacketSize = bt2BtPacketSize;
        }
        if (debug) Log.w(STAG, "setBt2BtPacketSize set to " + Settings.Bluetooth.bt2BtPacketSize);
    }

    static void setBtLatencyMs(int btLatencyMs) {
        if (btLatencyMs < 0) {
            Settings.Bluetooth.btLatencyMs = SettingsDefault.Bluetooth.btLatencyMs;
            if (debug) Log.e(STAG, "setBtLatencyMs illegal value, set to default");
        } else {
            Settings.Bluetooth.btLatencyMs = btLatencyMs;
        }
        if (debug) Log.w(STAG, "setBtLatencyMs set to " + Settings.Bluetooth.btLatencyMs);
    }

    static void setBtSinglePacket(boolean btSinglePacket) {
        Settings.Bluetooth.btSinglePacket = btSinglePacket;
        if (debug) Log.w(STAG, "setBtSinglePacket set to " + Settings.Bluetooth.btSinglePacket);
    }

    public static void setBtChosenAddr(String btChosenAddr) {

    }


}
