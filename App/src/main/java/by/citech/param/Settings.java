package by.citech.param;

import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class Settings {

    //---------------- COMMON

    public static final int bluetoothMessageSize = 16; // bytes in one BT message
    public static final int netSendSize = bluetoothMessageSize * 40;
    public static final DataSource dataSource = DataSource.BLUETOOTH;
//  public static final DataSource dataSource = DataSource.MICROPHONE;
    public static final boolean debug = true;
    public static final int bufferSize = 3000;
    public static final boolean testSendOneOnCall = false;

    //---------------- AUDIO COMMON

    public static final int audioRate = 8000;
    public static final int audioEncoding = AudioFormat.ENCODING_PCM_8BIT;

    //---------------- AUDIO IN

    public static final int audioInChannel = AudioFormat.CHANNEL_IN_MONO;
//  public static final int audioInBuffersize = AudioTrack.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
//  public static final int audioInBuffersize = audioOutBuffersize;
    public static final int audioInBuffersize = 10000;
    public static final int audioSource = MediaRecorder.AudioSource.MIC;

    //---------------- AUDIO OUT

    public static final int audioOutChannel = AudioFormat.CHANNEL_OUT_MONO;
//  public static final int audioOutBuffersize = AudioRecord.getMinBufferSize(audioRate, AUDIO_CHANNEL, audioEncoding) * 12;
    public static final int audioOutBuffersize = 10000;
    public static final int audioMode = AudioTrack.MODE_STREAM;
    public static final int audioContentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//  public static final int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_EARPIECE;
//  public static final int audioTarget = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER;
    public static final int audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;  // динамик
//  public static final int audioUsage = AudioAttributes.USAGE_MEDIA;  // спикер

    //---------------- NETWORK

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
