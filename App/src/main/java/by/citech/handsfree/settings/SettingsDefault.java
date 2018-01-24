package by.citech.handsfree.settings;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import by.citech.handsfree.codec.audio.EAudioCodecType;

public class SettingsDefault {

        public static class TypeName {
            public static final String opMode = "opMode";
            public static final String btLatencyMs = "btLatencyMs";
            public static final String btSinglePacket = "btSinglePacket";
            public static final String bt2BtPacketSize = "bt2BtPacketSize";
            public static final String bt2NetFactor = "bt2NetFactor";
            public static final String audioCodecType = "audioCodecType";
            public static String btChosenAddr;
        }

        public static class Common {
            public static final int threadNumber = 2;
            public static final EDataSource dataSource = EDataSource.BLUETOOTH;
            public static final EOpMode opMode = EOpMode.Normal;
            public static final boolean showTraffic = true;
            public static final int bt2NetFactor = 25;
            public static final int audioIn2BtFactor = 1;
            public static final int bt2AudioOutFactor = 1;
        }

        public static class Bluetooth {
            public static final int btNumberedBytePosition = 23;
            public static final boolean btSignificantAll = true;  // все байты значащие
            public static final boolean btSinglePacket = false;  // если возможно, не используем буфферизацию
            public static final int btAudioMsPerPacket = 10;  // миллисекунд звука в одном BT2BT-пакете
            public static final int bt2BtPacketSize = 20;  // bytes in one BT message
            public static final int btSignificantBytes = 20;  // кол-во значащих байтов данных в BT2BT-пакете
            public static final int btRsvdBytesOffset = 20;  // позиция начала незначащих байтов данных в BT2BT-пакете
            public static final int btFactor = Common.bt2NetFactor;  // кол-во буфферизированных пакетов BT2BT, отправляемое на BT
            public static final int btLatencyMs = 7;  // минимальный Thread.sleep между отправкой BT2BT-пакетов
            public static final int btSendSize = btSignificantBytes * btFactor;  // кол-во принятых извне полезных байт, к-е подходит для BT
            public static final int btAudioMsPerNetSendSize = btAudioMsPerPacket * btFactor;  // миллисекунд звука в одном BT2NET-пакете
            public static final int bt2NetSendSizeUncut = bt2BtPacketSize * btFactor;  // кол-во байт, к-е буфферизизируются перед отправкой в сеть
            public static final int btMtuSize = 20;  // запрашиваемый размер BT2BT-пакета
            public static String btChosenAddr;
        }

        public static class AudioCommon {
            public static final boolean audioSingleFrame = true;
            public static final boolean audioBuffIsShorts = true;
            public static final EAudioCodecType audioCodecType = EAudioCodecType.Sit_2_1_java;
            public static final int audioRate = 8000;
            public static final int audioBuffSizeBytes = 16000;
            public static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        }

        public static class AudioIn {
            public static final int audioInChannel = AudioFormat.CHANNEL_IN_MONO;
            public static final int audioInBuffersize = 10000;
            public static final int audioSource = MediaRecorder.AudioSource.MIC;
        }

        public static class AudioOut {
            public static final int audioOutChannel = AudioFormat.CHANNEL_OUT_MONO;
            public static final int audioOutBuffersize = 10000;
            public static final int audioMode = AudioTrack.MODE_STREAM;
            public static final int audioStreamType = AudioManager.STREAM_VOICE_CALL;
            public static final int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
            public static final int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
        }

        public static class Network {
            public static final boolean netSignificantAll = Bluetooth.btSignificantAll;
            public static final int netChunkSize = Bluetooth.bt2BtPacketSize;
            public static final int netChunkSignificantBytes = Bluetooth.btSignificantBytes;
            public static final int netChunkRsvdBytesOffset = Bluetooth.btRsvdBytesOffset;
            public static final int netFactor = Bluetooth.btFactor;
            public static final int netSendSize = netChunkSignificantBytes * netFactor;
            public static final String serverRemoteIpAddress = "192.168.0.126";
            public static final int serverLocalPortNumber = 8080;
            public static final int serverRemotePortNumber = 8080;
            public static final boolean reconnectOnFail = false;
            public static final long clientReadTimeout = 500000;
            public static final int serverTimeout = 500000;
            public static final long connectTimeout = 500000;
            public static final int storageMaxSize = 100;
            public static final boolean ipv4 = true;
            public static final boolean storageWriteOnOverflow = true;
        }

}
