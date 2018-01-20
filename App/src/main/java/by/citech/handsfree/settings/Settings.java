package by.citech.handsfree.settings;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.category.ISettingCategory;

public class Settings {

    static {
        Log.w("WSD_SETTINGS", "static initializer");
    }

    {
        Log.w("WSD_SETTINGS", "non-static initializer");
    }

    //--------------------- singleton

    private static volatile Settings instance = null;

    private Settings() {
    }

    public static Settings getInstance() {
        if (instance == null) {
            synchronized (Settings.class) {
                if (instance == null) {
                    instance = new Settings();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public Common getCommon() {
        return Common.getInstance();
    }

    public static boolean subscribe(SettingsSubscriber settingsSubscriber) {
        return false;
    }

    //---------------- Common

    public static class Common
            implements ISettingsReset {

        private static final String STAG = Tags.SettingCommon;
        private static int objCount;
        private final String TAG;
        private final int objNumber;
        static {objCount = 0;}
        {objCount++; objNumber = objCount; TAG = STAG + " " + objNumber;}
        private static volatile Common instance = null;
        private Common() {}
        private static Common getInstance() {
            if (instance == null) synchronized (Common.class) {if (instance == null) instance = new Common();}
            return instance;
        }

        @Override
        public boolean resetSettings() {
            setThreadNumber(SettingsDefault.Common.threadNumber);
            setDataSource(SettingsDefault.Common.dataSource);
            setOpMode(SettingsDefault.Common.opMode);
            setShowTraffic(SettingsDefault.Common.showTraffic);
            setDebug(SettingsDefault.Common.debug);
            setAudioIn2BtFactor(SettingsDefault.Common.audioIn2BtFactor);
            setBt2NetFactor(SettingsDefault.Common.bt2NetFactor);
            setBt2AudioOutFactor(SettingsDefault.Common.bt2AudioOutFactor);
            return false;
        }

        @Override
        public boolean resetSetting(ISettingCategory iSettingCategory) {
            return false;
        }

        public void setThreadNumber(Integer threadNumber) {this.threadNumber = threadNumber;}
        public void setDataSource(EDataSource dataSource) {this.dataSource = dataSource;}
        public void setOpMode(EOpMode opMode) {this.opMode = opMode;}
        public void setShowTraffic(Boolean showTraffic) {this.showTraffic = showTraffic;}
        public void setDebug(Boolean debug) {this.debug = debug;}
        public void setAudioIn2BtFactor(Integer audioIn2BtFactor) {this.audioIn2BtFactor = audioIn2BtFactor;}
        public void setBt2NetFactor(Integer bt2NetFactor) {this.bt2NetFactor = bt2NetFactor;}
        public void setBt2AudioOutFactor(Integer bt2AudioOutFactor) {this.bt2AudioOutFactor = bt2AudioOutFactor;}
        public void setStorageMaxSize(Integer storageMaxSize) {this.storageMaxSize = storageMaxSize;}

        public Integer getThreadNumber() {
            return Settings.threadNumber;
//            return threadNumber;
        }
        public EDataSource getDataSource() {return dataSource;}
        public EOpMode getOpMode() {
            return Settings.opMode;
//            return opMode;
        }
        public Boolean getShowTraffic() {return showTraffic;}
        public Boolean getDebug() {return debug;}
        public Integer getAudioIn2BtFactor() {return audioIn2BtFactor;}
        public Integer getBt2NetFactor() {return bt2NetFactor;}
        public Integer getBt2AudioOutFactor() {return bt2AudioOutFactor;}
        public Integer getStorageMaxSize() {return storageMaxSize;}

        private Integer threadNumber;
        private EDataSource dataSource;
        private EOpMode opMode;
        private Boolean showTraffic;
        private Boolean debug;
        private Integer audioIn2BtFactor;
        private Integer bt2NetFactor;
        private Integer bt2AudioOutFactor;
        private Integer storageMaxSize;

    }

    public static int threadNumber = 3;
    public static EDataSource dataSource = EDataSource.BLUETOOTH;
//  public static EDataSource dataSource = EDataSource.MICROPHONE;
    public static EOpMode opMode = EOpMode.Normal;
    public static boolean showTraffic = true;
    public static boolean debug = true;
    public static int bt2NetFactor = 25;  // кол-во буфферизированных пакетов BT2BT, отправляемое в сеть (BT2NET-пакет)
    public static int audioIn2BtFactor = 1;  // кол-во буфферизированных пакетов BT2BT, принимаемое от аудиовхода
    public static int bt2AudioOutFactor = 1;  // кол-во буфферизированных пакетов BT2BT, отправляемое на аудиовыход
    public static int storageMaxSize = 100;

    //---------------- Bluetooth

    private class Bluetooth {
    }

    public static boolean btSignificantAll = true;  // все байты значащие
    public static boolean btSinglePacket = false;  // если возможно, не используем буфферизацию
    public static int btAudioMsPerPacket = 10;  // миллисекунд звука в одном BT2BT-пакете
    public static int bt2BtPacketSize = 20;  // bytes in one BT message
    public static int btSignificantBytes = 20;  // кол-во значащих байтов данных в BT2BT-пакете
    public static int btRsvdBytesOffset = 20;  // позиция начала незначащих байтов данных в BT2BT-пакете
    public static int btFactor = bt2NetFactor;  // кол-во буфферизированных пакетов BT2BT, отправляемое на BT
    public static int btLatencyMs = 7;  // минимальный Thread.sleep между отправкой BT2BT-пакетов
    public static int btSendSize = btSignificantBytes * btFactor;  // кол-во принятых извне полезных байт, к-е подходит для BT
    public static int btAudioMsPerNetSendSize = btAudioMsPerPacket * bt2NetFactor;  // миллисекунд звука в одном BT2NET-пакете
    public static int bt2NetSendSizeUncut = bt2BtPacketSize * bt2NetFactor;  // кол-во байт, к-е буфферизизируются перед отправкой в сеть
    public static int btMtuSize = 80;  // запрашиваемый размер BT2BT-пакета
    public static String deviceAddressPrefix = "54:6C:0E";

    //---------------- AudioCommon

    private class AudioCommon {
    }

    public static boolean audioSingleFrame = true;
    public static boolean audioBuffIsShorts = true;
    public static EAudioCodecType audioCodecType = EAudioCodecType.Sit_2_1_java;
    public static int audioRate = 8000;
    public static int audioBuffSizeBytes = 16000;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//  public static int audioEncoding = AudioFormat.ENCODING_PCM_8BIT;

    //---------------- AudioIn

    private class AudioIn {
    }

    public static int audioInChannel = AudioFormat.CHANNEL_IN_MONO;
//  public static int audioInBuffersize = AudioTrack.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
//  public static int audioInBuffersize = audioOutBuffersize;
    public static int audioInBuffersize = 10000;
    public static int audioSource = MediaRecorder.AudioSource.MIC;

    //---------------- AudioOut

    private class AudioOut {
    }

    public static int audioOutChannel = AudioFormat.CHANNEL_OUT_MONO;
//  public static int audioOutBuffersize = AudioRecord.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
    public static int audioOutBuffersize = 10000;
    public static int audioMode = AudioTrack.MODE_STREAM;
//  public static int audioStreamType = AudioManager.STREAM_VOICE_CALL;
    public static int audioStreamType = AudioManager.STREAM_MUSIC;
    public static int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
//  public static int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;  // разговорный динамик
    public static int audioUsage = AudioAttributes.USAGE_MEDIA;  // громкая связь

    //---------------- Network

    private class Network {
    }

    public static boolean netSignificantAll = btSignificantAll;
    public static int netChunkSize = bt2BtPacketSize;
    public static int netChunkSignificantBytes = btSignificantBytes;
    public static int netChunkRsvdBytesOffset = btRsvdBytesOffset;
    public static int netFactor = btFactor;
    public static int netSendSize = netChunkSignificantBytes * netFactor;
    public static String serverRemoteIpAddress = "192.168.0.126";
    public static int serverLocalPortNumber = 8080;
    public static int serverRemotePortNumber = 8080;
    public static boolean reconnectOnFail = false;
    public static long clientReadTimeout = 500000;
    public static int serverTimeout = 500000;
    public static long connectTimeout = 500000;
    public static boolean isIpv4Used = true;
    public static boolean storageWriteOnOverflow = true;

}
