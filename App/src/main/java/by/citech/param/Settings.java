package by.citech.param;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import by.citech.codec.audio.AudioCodecType;

public class Settings {

    static {
        Log.w("SETTINGS", "static initializer");
    }

    {
        Log.w("SETTINGS", "non-static initializer");
    }

    //---------------- COMMON

    public static DataSource dataSource = DataSource.BLUETOOTH;
//  public static DataSource dataSource = DataSource.MICROPHONE;
    public static OpMode opMode = OpMode.Normal;
//  public static OpMode opMode = OpMode.AudIn2Bt;
//  public static OpMode opMode = OpMode.Bt2AudOut;
//  public static OpMode opMode = OpMode.AudIn2AudOut;
//  public static OpMode opMode = OpMode.Bt2Bt;
//  public static OpMode opMode = OpMode.Net2Net; //TODO: реализовать
//  public static OpMode opMode = OpMode.Record;
    public static boolean showTraffic = true;
    public static boolean debug = true;
    public static boolean testSendOneOnCall = false;

    //---------------- BLUETOOTH

    public static boolean btSinglePacket = false;  // если возможно, не используем буфферизацию
    public static int btAudioMsPerPacket = 10;  // миллисекунд звука в одном BT2BT-пакете
    public static int bt2btPacketSize = 16;  // bytes in one BT message
    public static int btSignificantBytes = 10;  // кол-во значащих байтов данных в BT2BT-пакете
    public static int btRsvdBytesOffset = 10;  // позиция начала незначащих байтов данных в BT2BT-пакете
    public static int bt2NetFactor = 90;  // кол-во буфферизированных пакетов BT2BT, отправляемое в сеть (BT2NET-пакет)
    public static int audioIn2BtFactor = 1;  // кол-во буфферизированных пакетов BT2BT, принимаемое от аудиовхода
    public static int bt2AudioOutFactor = 1;  // кол-во буфферизированных пакетов BT2BT, отправляемое на аудиовыход
    public static int btFactor = bt2NetFactor;  // кол-во буфферизированных пакетов BT2BT, отправляемое на BT
    public static int btLatencyMs = 9;  // минимальный Thread.sleep между отправкой BT2BT-пакетов
    public static int btSendSize = btSignificantBytes * btFactor;  // кол-во принятых извне полезных байт, к-е подходит для BT
    public static int btAudioMsPerNetSendSize = btAudioMsPerPacket * bt2NetFactor;  // миллисекунд звука в одном BT2NET-пакете
    public static int bt2NetSendSizeUncut = bt2btPacketSize * bt2NetFactor;  // кол-во байт, к-е буфферизизируются перед отправкой в сеть
    public static int btMtuSize = 80;  // запрашиваемый размер BT2BT-пакета

    //---------------- AUDIO COMMON

    public static boolean audioSingleFrame = true;
    public static boolean audioBuffIsShorts = true;
    public static AudioCodecType audioCodecType = AudioCodecType.Sit_2_1_java;
    public static int audioRate = 8000;
    public static int audioBuffSizeBytes = 16000;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//  public static int audioEncoding = AudioFormat.ENCODING_PCM_8BIT;

    //---------------- AUDIO IN

    public static int audioInChannel = AudioFormat.CHANNEL_IN_MONO;
//  public static int audioInBuffersize = AudioTrack.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
//  public static int audioInBuffersize = audioOutBuffersize;
    public static int audioInBuffersize = 10000;
    public static int audioSource = MediaRecorder.AudioSource.MIC;

    //---------------- AUDIO OUT

    public static int audioOutChannel = AudioFormat.CHANNEL_OUT_MONO;
//  public static int audioOutBuffersize = AudioRecord.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
    public static int audioOutBuffersize = 10000;
    public static int audioMode = AudioTrack.MODE_STREAM;
    public static int audioStreamType = AudioManager.STREAM_VOICE_CALL;
    public static int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
    public static int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;  // разговорный динамик
//  public static int audioUsage = AudioAttributes.USAGE_MEDIA;  // громкая связь

    //---------------- NETWORK

    public static int netChunkSignificantBytes = btSignificantBytes;
    public static int netChunkRsvdBytesOffset = btRsvdBytesOffset;
    public static int netSendSize = btSendSize;
    public static String serverRemoteIpAddress = "192.168.0.126";
    public static int serverLocalPortNumber = 8080;
    public static int serverRemotePortNumber = 8080;
    public static boolean reconnect = false;
    public static long clientReadTimeout = 500000;
    public static int serverTimeout = 500000;
    public static long connectTimeout = 500000;
    public static int storageMaxSize = 100;
    public static boolean ipv4 = true;
    public static boolean storageWriteOnOverflow = true;

}
