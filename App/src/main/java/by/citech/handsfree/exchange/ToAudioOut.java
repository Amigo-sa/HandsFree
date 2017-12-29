package by.citech.handsfree.exchange;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class ToAudioOut
        implements ITransmitterCtrl, ITransmitter, ISettingsCtrl, IPrepareObject {

    private static final String TAG = Tags.TO_AUDOUT;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private boolean audioBuffIsShorts;
    private int audioStreamType;
    private int audioUsage;
    private int audioContentType;
    private int audioEncoding;
    private int audioRate;
    private int audioOutChannel;
    private int audioMode;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private byte[] buffBytes;
    private short[] buffShorts;
    private int buffCnt;
    private AudioTrack audioTrack;
    private boolean isStreaming;

    {
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        takeSettings();
        applySettings(null);
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        if (audioBuffIsShorts) {
            buffShorts = new short[audioBuffSizeShorts];
        } else {
            buffBytes = new byte[audioBuffSizeBytes];
        }
        return true;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        audioBuffIsShorts = Settings.audioBuffIsShorts;
        audioStreamType = Settings.audioStreamType;
        audioUsage = Settings.audioUsage;
        audioContentType = Settings.audioContentType;
        audioEncoding = Settings.audioEncoding;
        audioRate = Settings.audioRate;
        audioOutChannel = Settings.audioOutChannel;
        audioMode = Settings.audioMode;
        audioBuffSizeBytes = Settings.audioSingleFrame
                ? (Settings.audioCodecType.getDecodedShortsSize() * 2)
                : Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
        return true;
    }

    //--------------------- ITransmitterCtrl

    @Override
    public void prepareStream(ITransmitter iTransmitter) throws Exception {
        if (debug) Log.i(TAG, "prepareRedirect");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (debug) Log.i(TAG, "prepareRedirect version HIGH");
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(audioUsage)
                            .setContentType(audioContentType)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(audioEncoding)
                            .setSampleRate(audioRate)
                            .setChannelMask(audioOutChannel)
                            .build())
                    .setBufferSizeInBytes(audioBuffSizeBytes)
                    .setTransferMode(audioMode)
                    .build();
        } else {
            if (debug) Log.i(TAG, "prepareRedirect version LOW");
            audioTrack = new AudioTrack(
                    audioStreamType,
                    audioRate,
                    audioOutChannel,
                    audioEncoding,
                    audioBuffSizeBytes,
                    audioMode
            );
        }
        Log.w(TAG, String.format(Locale.US, "redirectOn parameters is:" +
                        " audioBuffIsShorts is %b," +
                        " audioRate is %d," +
                        " audioBuffSizeBytes is %d," +
                        " audioBuffSizeShorts is %d",
                audioBuffIsShorts,
                audioRate,
                audioBuffSizeBytes,
                audioBuffSizeShorts
        ));
        if (debug) Log.i(TAG, "prepareRedirect done");
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        streamOff();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        if (debug) Log.i(TAG, "finishStream done");
    }

    @Override
    public void streamOn() {
        if (isStreaming || (audioTrack == null)) {
            Log.e(TAG, "streamOn already redirecting or audioTrack is null");
            return;
        }
        isStreaming = true;
        audioTrack.play();
        if (debug) Log.i(TAG, "streamOn done");
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        buffCnt = 0;
        isStreaming = false;
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }
        }
        if (debug) Log.i(TAG, "streamOff done");
    }

    //--------------------- IReceiver

    @Override
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData byte[]");
        int dataLength;
        if (data == null) {
            if (debug) Log.w(TAG, "sendData byte[] data is null, return");
            return;
        } else {
            dataLength = data.length;
            if (!isStreaming || audioBuffIsShorts || dataLength == 0) {
                if (debug) Log.w(TAG, "sendData byte[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        try {
            System.arraycopy(data, 0, buffBytes, buffCnt, dataLength);
        } catch (Exception e) {
            if (debug) Log.w(TAG, String.format(Locale.US,
                    "sendData byte[] buffCnt is %d, dataLength is %d, buffBytes is null: %b",
                    buffCnt, dataLength, buffBytes == null));
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeBytes) {
            if (debug) Log.i(TAG, "sendData byte[] buffered bytes to play: " + buffCnt);
            audioTrack.write(buffBytes, 0, audioBuffSizeBytes);
            buffCnt = 0;
        }
    }

    @Override
    public void sendData(short[] data) {
        if (buffCnt == 0) {
            if (debug) Log.i(TAG, "sendData short[] buffCnt = 0, first receive");
        }
        int dataLength;
        if (data == null) {
            if (debug) Log.w(TAG, "sendData short[] data is null, return");
            return;
        } else {
            dataLength = data.length;
            if (!isStreaming || !audioBuffIsShorts || dataLength == 0) {
                if (debug) Log.w(TAG, "sendData short[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        try {
            System.arraycopy(data, 0, buffShorts, buffCnt, dataLength);
        } catch (Exception e) {
            if (debug) Log.w(TAG, String.format(Locale.US,
                    "sendData short[] buffCnt is %d, dataLength is %d, buffShorts is null: %b",
                    buffCnt, dataLength, buffShorts == null));
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeShorts) {
            if (debug) Log.i(TAG, "sendData short[] buffered bytes to play: " + buffCnt);
            audioTrack.write(buffShorts, 0, audioBuffSizeShorts);
            buffCnt = 0;
        } else {
            if (debug) Log.i(TAG, "sendData short[] buffered bytes: " + buffCnt);
        }
    }

}
