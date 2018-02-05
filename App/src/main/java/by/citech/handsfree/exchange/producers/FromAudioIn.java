package by.citech.handsfree.exchange.producers;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.ESeverityLevel;
import timber.log.Timber;

public class FromAudioIn
        implements IStreamer {

    private final String TAG = Tags.Audio;
    private final boolean debug = Settings.debug;

    //--------------------- preparation

    private boolean audioBuffIsShorts;
    private int audioSource;
    private int audioRate;
    private int audioInChannel;
    private int audioEncoding;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private AudioRecord recorder;
    private boolean isStreaming;
    private boolean isFinished;
    private boolean isPrepared;
    private IRxComplex iRxComplex;

    {
        audioBuffIsShorts = Settings.AudioCommon.audioBuffIsShorts;
        audioSource = Settings.AudioIn.audioSource;
        audioRate = Settings.AudioCommon.audioRate;
        audioInChannel = Settings.AudioIn.audioInChannel;
        audioEncoding = Settings.AudioCommon.audioEncoding;
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
        } else if (receiver == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        } else {
            Timber.i("prepareStream");
            this.iRxComplex = receiver;
        }
        recorder = new AudioRecord(
                audioSource,
                audioRate,
                audioInChannel,
                audioEncoding,
                audioBuffSizeBytes);
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
        if (recorder != null) {
            isPrepared = true;
        }
    }

    @Override
    public void finishStream() {
        Timber.i("finishStream");
        isFinished = true;
        streamOff();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        iRxComplex = null;
    }

    @Override
    public void streamOff() {
        Timber.i("streamOff");
        isStreaming = false;
        if (recorder != null) {
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                recorder.stop();
            }
        }
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

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            Timber.i("streamOn");
        }
        isStreaming = true;
        recorder.startRecording();
        while (isStreaming() && isReadyToStream()) {
            if (audioBuffIsShorts) streamShorts();
            else                   streamBytes();
        }
        Timber.i("streamOn done");
    }

    //--------------------- main

    private void streamShorts() {
        iRxComplex.sendData(fillShortsBuff(audioBuffSizeShorts));
    }

    private void streamBytes() {
        iRxComplex.sendData(fillBytesBuff(audioBuffSizeBytes));
    }

    private byte[] fillBytesBuff(int readLeft) {
        Timber.d("fillBytesBuff");
        byte[] buffer = new byte[audioBuffSizeBytes];
        int readCount = 0;
        int readOffset = 0;
        while (isStreaming() && isReadyToStream() && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        Timber.d("fillBytesBuff done");
        return buffer;
    }

    private short[] fillShortsBuff(int readLeft) {
        Timber.d("fillShortsBuff");
        short[] buffer = new short[audioBuffSizeShorts];
        int readCount = 0;
        int readOffset = 0;
        while (isStreaming() && isReadyToStream() && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        Timber.d("fillShortsBuff done");
        return buffer;
    }

}
