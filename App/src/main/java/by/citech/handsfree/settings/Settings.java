package by.citech.handsfree.settings;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import by.citech.handsfree.codec.audio.EAudioCodecType;

import static by.citech.handsfree.settings.Settings.Common.bt2NetFactor;

public class Settings {

    public static final boolean debug = true;

    //---------------- Common

    public static class Common {
        public static int toolbarBaseSize = 40;
        public static float toolbarMessageToPrefix = 0.6f;
        public static int threadNumber = 3;
        public static EDataSource dataSource = EDataSource.BLUETOOTH;
        public static EOpMode opMode = EOpMode.Normal;
        public static boolean showTraffic = true;
        public static int bt2NetFactor = 25;  // кол-во буфферизированных пакетов BT2BT, отправляемое в сеть (BT2NET-пакет)
        public static int audioIn2BtFactor = 1;  // кол-во буфферизированных пакетов BT2BT, принимаемое от аудиовхода
        public static int bt2AudioOutFactor = 1;  // кол-во буфферизированных пакетов BT2BT, отправляемое на аудиовыход
        public static int storageMaxSize = 100;
    }

    //---------------- Bluetooth

    public static class Bluetooth {
        public static int btNumberedBytePosition = 0;
        public static int btNumberedBytesToIntStart = 20;
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
        public static String btChosenAddr = "";
    }

    //---------------- AudioCommon

    public static class AudioCommon {
        public static boolean audioSingleFrame = true;
        public static boolean audioBuffIsShorts = true;
        public static EAudioCodecType audioCodecType = EAudioCodecType.Sit_2_1_java;
        public static int audioRate = 8000;
        public static int audioBuffSizeBytes = 16000;
        public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    }

    //---------------- AudioIn

    public static class AudioIn {
        public static int audioInChannel = AudioFormat.CHANNEL_IN_MONO;
        public static int audioInBuffersize = 10000;
        public static int audioSource = MediaRecorder.AudioSource.MIC;
    }

    //---------------- AudioOut

    public static class AudioOut {
        public static int audioOutChannel = AudioFormat.CHANNEL_OUT_MONO;
        public static int audioOutBuffersize = AudioIn.audioInBuffersize;
        public static int audioMode = AudioTrack.MODE_STREAM;
        public static int audioStreamType = AudioManager.STREAM_VOICE_CALL;
//      public static int audioStreamType = AudioManager.STREAM_MUSIC;
        public static int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//      public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
        public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
        public static int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;  // разговорный динамик
//      public static int audioUsage = AudioAttributes.USAGE_MEDIA;  // громкая связь
    }

    //---------------- Network

    public static class Network {
        public static boolean netSignificantAll = Bluetooth.btSignificantAll;
        public static int netChunkSize = Bluetooth.bt2BtPacketSize;
        public static int netChunkSignificantBytes = Bluetooth.btSignificantBytes;
        public static int netChunkRsvdBytesOffset = Bluetooth.btRsvdBytesOffset;
        public static int netFactor = Bluetooth.btFactor;
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

}
