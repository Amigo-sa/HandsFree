package by.citech.param;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Settings {
    public static int bluetoothMessageSize = 16; // bytes in one BT message

    public static int minNetSendSize = bluetoothMessageSize - 1;
    public static DataSource dataSource = DataSource.BLUETOOTH;
//  public static DataSource dataSource = DataSource.MICROPHONE;
    public static boolean debug = true;
    public static int bufferSize = 3000;
    public static boolean testSendOneOnCall = false;

    //---------------- AUDIO COMMON

    public static int audioRate = 8000;
    public static int audioEncoding = AudioFormat.ENCODING_PCM_8BIT;

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
    public static int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
//  public static int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
    public static int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;  // динамик
//  public static int audioUsage = AudioAttributes.USAGE_MEDIA;  // спикер

    //---------------- NETWORK

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
