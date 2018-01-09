package by.citech.handsfree.exchange.consumers;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ESeverityLevel;

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
    private boolean isPrepared;
    private boolean isFinished;

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
    public boolean applySettings(ESeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
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
    public void prepareStream(ITransmitter receiver) throws Exception {
        if (isFinished) {
            if (debug) Log.w(TAG, "prepareStream stream is finished, return");
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (debug) Log.i(TAG, "prepareStream version HIGH");
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
            if (debug) Log.i(TAG, "prepareStream version LOW");
            audioTrack = new AudioTrack(
                    audioStreamType,
                    audioRate,
                    audioOutChannel,
                    audioEncoding,
                    audioBuffSizeBytes,
                    audioMode
            );
        }
        if (debug) Log.w(TAG, String.format(Locale.US, "prepareStream parameters is:" +
                        " audioBuffIsShorts is %b," +
                        " audioRate is %d," +
                        " audioBuffSizeBytes is %d," +
                        " audioBuffSizeShorts is %d",
                audioBuffIsShorts,
                audioRate,
                audioBuffSizeBytes,
                audioBuffSizeShorts
        ));
        if (audioTrack != null) {
            isPrepared = true;
        }
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        isFinished = true;
        streamOff();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        if (debug) Log.i(TAG, "finishStream done");
    }

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            if (debug) Log.i(TAG, "streamOn");
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

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public boolean isReadyToStream() {
        if (isFinished) {
            if (debug) Log.w(TAG, "isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            if (debug) Log.w(TAG, "isReadyToStream not prepared");
            return false;
        } else {
            return true;
        }
    }

    //--------------------- IReceiver

    @Override
    public void sendData(byte[] data) {
        if (buffCnt == 0) {
            if (debug) Log.i(TAG, "sendData data[] buffCnt = 0, first receive");
        }
        int dataLength;
        if (data == null) {
            if (debug) Log.w(TAG, "sendData byte[] data is null, return");
            return;
        } else {
            dataLength = data.length;
            if (audioBuffIsShorts || dataLength == 0) {
                if (debug) Log.w(TAG, "sendData byte[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        if (!isStreaming() || !isReadyToStream()){
            return;
        } else {
            if (debug) Log.i(TAG, "sendData byte[]");
        }
        if (buffBytes == null) {
            buffBytes = new byte[audioBuffSizeBytes];
        }
        try {
            System.arraycopy(data, 0, buffBytes, buffCnt, dataLength);
        } catch (Exception e) {
            if (debug) Log.w(TAG, String.format(Locale.US,
                    "sendData byte[] buffCnt is %d, dataLength is %d",
                    buffCnt, dataLength));
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeBytes) {
            if (isReadyToStream()) {
                if (debug) Log.i(TAG, "sendData byte[] buffered bytes to play: " + buffCnt);
                audioTrack.write(buffBytes, 0, audioBuffSizeBytes);
                buffBytes = null;
                buffCnt = 0;
            }
        } else {
            if (debug) Log.i(TAG, "sendData byte[] buffered bytes: " + buffCnt);
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
            if (!audioBuffIsShorts || dataLength == 0) {
                if (debug) Log.w(TAG, "sendData short[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        if (!isStreaming() || !isReadyToStream()){
            return;
        } else {
            if (debug) Log.i(TAG, "sendData short[]");
        }
        if (buffShorts == null) {
            buffShorts = new short[audioBuffSizeShorts];
        }
        try {
            System.arraycopy(data, 0, buffShorts, buffCnt, dataLength);
        } catch (Exception e) {
            if (debug) Log.w(TAG, String.format(Locale.US,
                    "sendData short[] buffCnt is %d, dataLength is %d",
                    buffCnt, dataLength));
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeShorts) {
            if (isReadyToStream()) {
                if (debug) Log.i(TAG, "sendData short[] buffered shorts to play: " + buffCnt);
                audioTrack.write(buffShorts, 0, audioBuffSizeShorts);
                buffShorts = null;
                buffCnt = 0;
            }
        } else {
            if (debug) Log.i(TAG, "sendData short[] buffered shorts: " + buffCnt);
        }
    }

}
