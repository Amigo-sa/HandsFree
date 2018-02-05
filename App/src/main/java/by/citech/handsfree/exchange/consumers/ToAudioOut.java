package by.citech.handsfree.exchange.consumers;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class ToAudioOut
        implements IStreamer, IRxComplex {

    private static final String TAG = Tags.ToAudioOut;
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
        audioBuffIsShorts = Settings.AudioCommon.audioBuffIsShorts;
        audioStreamType = Settings.AudioOut.audioStreamType;
        audioUsage = Settings.AudioOut.audioUsage;
        audioContentType = Settings.AudioOut.audioContentType;
        audioEncoding = Settings.AudioCommon.audioEncoding;
        audioRate = Settings.AudioCommon.audioRate;
        audioOutChannel = Settings.AudioOut.audioOutChannel;
        audioMode = Settings.AudioOut.audioMode;
        audioBuffSizeBytes = Settings.AudioCommon.audioSingleFrame
                ? (Settings.AudioCommon.audioCodecType.getDecodedShortsSize() * 2)
                : Settings.AudioCommon.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
    }

    //--------------------- IStreamer

    @Override
    public void prepareStream(IRxComplex receiver) throws Exception {
        if (isFinished) {
            Timber.w("prepareStream stream is finished, return");
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Timber.i("prepareStream version HIGH");
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
            Timber.i("prepareStream version LOW");
            audioTrack = new AudioTrack(
                    audioStreamType,
                    audioRate,
                    audioOutChannel,
                    audioEncoding,
                    audioBuffSizeBytes,
                    audioMode
            );
        }
        Timber.w(String.format(Locale.US, "prepareStream parameters is:" +
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
        Timber.i("finishStream");
        isFinished = true;
        streamOff();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        Timber.i("finishStream done");
    }

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            Timber.i("streamOn");
        }
        isStreaming = true;
        audioTrack.play();
        Timber.i("streamOn done");
    }

    @Override
    public void streamOff() {
        Timber.i("streamOff");
        buffCnt = 0;
        isStreaming = false;
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
            }
        }
        Timber.i("streamOff done");
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @Override
    public boolean isReadyToStream() {
        if (isFinished) {
            Timber.w("isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            Timber.w("isReadyToStream not prepared");
            return false;
        } else {
            return true;
        }
    }

    //--------------------- IReceiver

    @Override
    public void sendData(byte[] data) {
        if (buffCnt == 0) {
            Timber.i("sendData data[] buffCnt = 0, first receive");
        }
        int dataLength;
        if (data == null) {
            Timber.w("sendData byte[] data is null, return");
            return;
        } else {
            dataLength = data.length;
            if (audioBuffIsShorts || dataLength == 0) {
                Timber.w("sendData byte[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        if (!isStreaming() || !isReadyToStream()){
            return;
        } else {
            Timber.i("sendData byte[]");
        }
        if (buffBytes == null) {
            buffBytes = new byte[audioBuffSizeBytes];
        }
        try {
            System.arraycopy(data, 0, buffBytes, buffCnt, dataLength);
        } catch (Exception e) {
            Timber.w("sendData byte[] buffCnt is %d, dataLength is %d",
                    buffCnt, dataLength);
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeBytes) {
            if (isReadyToStream()) {
                Timber.i("sendData byte[] buffered bytes to play: %s", buffCnt);
                audioTrack.write(buffBytes, 0, audioBuffSizeBytes);
                buffBytes = null;
                buffCnt = 0;
            }
        } else {
            Timber.i("sendData byte[] buffered bytes: " + buffCnt);
        }
    }

    @Override
    public void sendData(short[] data) {
        if (buffCnt == 0) {
            Timber.i("sendData short[] buffCnt = 0, first receive");
        }
        int dataLength;
        if (data == null) {
            Timber.w("sendData short[] data is null, return");
            return;
        } else {
            dataLength = data.length;
            if (!audioBuffIsShorts || dataLength == 0) {
                Timber.w("sendData short[]" + StatusMessages.ERR_PARAMETERS);
                return;
            }
        }
        if (!isStreaming() || !isReadyToStream()){
            return;
        } else {
            Timber.i("sendData short[]");
        }
        if (buffShorts == null) {
            buffShorts = new short[audioBuffSizeShorts];
        }
        try {
            System.arraycopy(data, 0, buffShorts, buffCnt, dataLength);
        } catch (Exception e) {
            Timber.w(String.format(Locale.US,
                    "sendData short[] buffCnt is %d, dataLength is %d",
                    buffCnt, dataLength));
            e.printStackTrace();
        }
        buffCnt = buffCnt + dataLength;
        if (buffCnt >= audioBuffSizeShorts) {
            if (isReadyToStream()) {
                Timber.i("sendData short[] buffered shorts to play: " + buffCnt);
                audioTrack.write(buffShorts, 0, audioBuffSizeShorts);
                buffShorts = null;
                buffCnt = 0;
            }
        } else {
            Timber.i("sendData short[] buffered shorts: " + buffCnt);
        }
    }

}
