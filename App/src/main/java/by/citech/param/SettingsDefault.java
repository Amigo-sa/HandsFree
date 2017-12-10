package by.citech.param;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import by.citech.codec.audio.AudioCodecType;

public class SettingsDefault {

        public static class Key {
            public static final String opMode = "opMode";
            public static final String btLatencyMs = "btLatencyMs";
            public static final String btSinglePacket = "btSinglePacket";
            public static final String bt2NetFactor = "bt2NetFactor";
            public static final String audioCodecType = "audioCodecType";
        }

        public static class Common {
            public static final int threadNumber = 2;
            public static final DataSource dataSource = DataSource.BLUETOOTH;
            public static final OpMode opMode = OpMode.Normal;
            public static final boolean showTraffic = true;
            public static final boolean debug = true;
            public static final boolean testSendOneOnCall = false;
        }

        public static class Bluetooth {
            public static final boolean btSinglePacket = false;
            public static final int btAudioMsPerPacket = 10;
            public static final int bt2btPacketSize = 16;
            public static final int btSignificantBytes = 10;
            public static final int btRsvdBytesOffset = 10;
            public static final int bt2NetFactor = 90;
            public static final int audioIn2BtFactor = 1;
            public static final int bt2AudioOutFactor = 1;
            public static final int btFactor = bt2NetFactor;
            public static final int btLatencyMs = 9;
            public static final int btSendSize = btSignificantBytes * btFactor;
            public static final int btAudioMsPerNetSendSize = btAudioMsPerPacket * bt2NetFactor;
            public static final int bt2NetSendSizeUncut = bt2btPacketSize * bt2NetFactor;
            public static final int btMtuSize = 80;
        }

        public static class AudioCommon {
            public static final boolean audioSingleFrame = true;
            public static final boolean audioBuffIsShorts = true;
            public static final AudioCodecType audioCodecType = AudioCodecType.Sit_2_1_java;
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
            public static final int netChunkSignificantBytes = Bluetooth.btSignificantBytes;
            public static final int netChunkRsvdBytesOffset = Bluetooth.btRsvdBytesOffset;
            public static final int netSendSize = Bluetooth.btSendSize;
            public static final String serverRemoteIpAddress = "192.168.0.126";
            public static final int serverLocalPortNumber = 8080;
            public static final int serverRemotePortNumber = 8080;
            public static final boolean reconnect = false;
            public static final long clientReadTimeout = 500000;
            public static final int serverTimeout = 500000;
            public static final long connectTimeout = 500000;
            public static final int storageMaxSize = 100;
            public static final boolean ipv4 = true;
            public static final boolean storageWriteOnOverflow = true;
        }

}
