package by.citech.handsfree.settings;

import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.util.BluetoothHelper;
import timber.log.Timber;

class Presetter {

    private static final boolean debug = Settings.debug;

    static void setOpMode(EOpMode opMode) {
        if (opMode != null) {
            switch (opMode) {
                case Bt2Bt:
                    Settings.Common.opMode = EOpMode.Bt2Bt;
                    Settings.Bluetooth.btSinglePacket = false;
                    Settings.Bluetooth.btFactor = Settings.Common.bt2NetFactor;
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
                    Settings.Bluetooth.btFactor = Settings.Common.bt2NetFactor;
                    break;
                case Net2Net:
                    setToDefault();
                    if (debug) Timber.e("setOpMode opMode is Net2Net (not implemented), set to default");
                    break;
                case Normal:
                    setToDefault();
                    break;
                default:
                    setToDefault();
                    if (debug) Timber.e("setOpMode no matches for opMode, set to default");
                    break;
            }
        } else {
            Settings.Common.opMode = SettingsDefault.Common.opMode;
            if (debug) Timber.e("setOpMode illegal value, set to default");
        }
        if (debug) Timber.w("setOpMode opMode set to %s", Settings.Common.opMode.getSettingName());
    }

    private static void setToDefault() {
        Settings.Common.opMode = SettingsDefault.Common.opMode;
        Settings.Bluetooth.btSinglePacket = false;
        Settings.Bluetooth.btSignificantAll = true;
        Settings.Bluetooth.btSignificantBytes = Settings.Bluetooth.bt2BtPacketSize;
        Settings.Network.netChunkSignificantBytes = Settings.Bluetooth.btSignificantBytes;
        Settings.Network.netChunkSize = Settings.Bluetooth.bt2BtPacketSize;
        Settings.Network.netSignificantAll = Settings.Bluetooth.btSignificantAll;
        Settings.Network.netFactor = Settings.Bluetooth.btFactor;
        Settings.Bluetooth.btSendSize =  Settings.Bluetooth.btSignificantBytes * Settings.Bluetooth.btFactor;
    }

    static void setAudioCodecType(EAudioCodecType audioCodecType) {
        if (audioCodecType == null) {
            Settings.AudioCommon.audioCodecType = SettingsDefault.AudioCommon.audioCodecType;
            if (debug) Timber.e("setAudioCodecType illegal value, set to default");
        } else Settings.AudioCommon.audioCodecType = audioCodecType;
        if (debug) Timber.w("audioCodecType set to %s", Settings.AudioCommon.audioCodecType.getSettingName());
    }

    static void setBt2NetFactor(int bt2NetFactor) {
        if (bt2NetFactor < 0) {
            Settings.Common.bt2NetFactor = SettingsDefault.Common.bt2NetFactor;
            if (debug) Timber.e("setBt2NetFactor illegal value, set to default");
        } else Settings.Common.bt2NetFactor = bt2NetFactor;
        if (debug) Timber.w("setBt2NetFactor set to %s", Settings.Common.bt2NetFactor);
    }

    static void setBt2BtPacketSize(int bt2BtPacketSize) {
        if (bt2BtPacketSize < 0) {
            Settings.Bluetooth.bt2BtPacketSize = SettingsDefault.Bluetooth.bt2BtPacketSize;
            if (debug) Timber.e("setBt2BtPacketSize illegal value, set to default");
        } else Settings.Bluetooth.bt2BtPacketSize = bt2BtPacketSize;
        if (debug) Timber.w("setBt2BtPacketSize set to %s", Settings.Bluetooth.bt2BtPacketSize);
    }

    static void setBtLatencyMs(int btLatencyMs) {
        if (btLatencyMs < 0) {
            Settings.Bluetooth.btLatencyMs = SettingsDefault.Bluetooth.btLatencyMs;
            if (debug) Timber.e("setBtLatencyMs illegal value, set to default");
        } else Settings.Bluetooth.btLatencyMs = btLatencyMs;
        if (debug) Timber.w("setBtLatencyMs set to %s", Settings.Bluetooth.btLatencyMs);
    }

    static void setBtSinglePacket(boolean btSinglePacket) {
        Settings.Bluetooth.btSinglePacket = btSinglePacket;
        if (debug) Timber.w("setBtSinglePacket set to %s", Settings.Bluetooth.btSinglePacket);
    }

    static void setBtChosenAddr(String btChosenAddr) {
        if (!BluetoothHelper.isValidAddr(btChosenAddr)) {
            Settings.Bluetooth.btChosenAddr = SettingsDefault.Bluetooth.btChosenAddr;
            if (debug) Timber.e("setBtChosenAddr illegal value, set to default");
        } else Settings.Bluetooth.btChosenAddr = btChosenAddr;
        if (debug) Timber.w("setBtChosenAddr set to %s", Settings.Bluetooth.btChosenAddr);
    }

}
