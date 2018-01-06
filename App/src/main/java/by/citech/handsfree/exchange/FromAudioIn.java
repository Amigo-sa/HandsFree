package by.citech.handsfree.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class FromAudioIn
        implements ITransmitterCtrl, IPrepareObject, ISettingsCtrl {

    private final String TAG = Tags.FROM_AUDIN;
    private final boolean debug = Settings.debug;

    //--------------------- preparation

    private boolean audioBuffIsShorts;
    private int audioSource;
    private int audioRate;
    private int audioInChannel;
    private int audioEncoding;
    private int audioBuffSizeBytes;
    private int audioBuffSizeShorts;
    private byte[] bytesBuffer;
    private short[] shortsBuffer;
    private AudioRecord recorder;
    private boolean isStreaming;
    private boolean isFinished;
    private boolean isPrepared;
    private ITransmitter iTransmitter;

    {
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        takeSettings();
        applySettings(null);
        return false;
    }

    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        audioBuffIsShorts = Settings.audioBuffIsShorts;
        audioSource = Settings.audioSource;
        audioRate = Settings.audioRate;
        audioInChannel = Settings.audioInChannel;
        audioEncoding = Settings.audioEncoding;
        audioBuffSizeBytes = Settings.audioSingleFrame
                ? (Settings.audioCodecType.getDecodedShortsSize() * 2)
                : Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        return true;
    }

    //--------------------- ITransmitterCtrl

    @Override
    public void prepareStream(ITransmitter iTransmitter) throws Exception {
        if (isFinished) {
            if (debug) Log.w(TAG, "prepareStream stream is finished, return");
            return;
        } else if (iTransmitter == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        } else {
            if (debug) Log.i(TAG, "prepareStream");
            this.iTransmitter = iTransmitter;
        }
        recorder = new AudioRecord(
                audioSource,
                audioRate,
                audioInChannel,
                audioEncoding,
                audioBuffSizeBytes);
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
        if (recorder != null) {
            isPrepared = true;
        }
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        isFinished = true;
        streamOff();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        iTransmitter = null;
        shortsBuffer = null;
        bytesBuffer = null;
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
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
            if (debug) Log.w(TAG, "isReadyToStream finished");
            return false;
        } else if (!isPrepared) {
            if (debug) Log.w(TAG, "isReadyToStream not prepared");
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
            if (debug) Log.i(TAG, "streamOn");
        }
        isStreaming = true;
        recorder.startRecording();
        while (isStreaming() && isReadyToStream()) {
            if (audioBuffIsShorts) streamShorts();
            else                   streamBytes();
        }
        if (debug) Log.i(TAG, "streamOn done");
    }

    //--------------------- main

    private void streamShorts() {
        iTransmitter.sendData(fillShortsBuff(audioBuffSizeShorts));
    }

    private void streamBytes() {
        iTransmitter.sendData(fillBytesBuff(audioBuffSizeBytes));
    }

    private byte[] fillBytesBuff(int readLeft) {
        if (debug) Log.d(TAG, "fillBytesBuff");
        byte[] buffer = new byte[audioBuffSizeBytes];
        int readCount = 0;
        int readOffset = 0;
        while (isStreaming() && isReadyToStream() && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        if (debug) Log.d(TAG, "fillBytesBuff done");
        return buffer;
    }

    private short[] fillShortsBuff(int readLeft) {
        if (debug) Log.d(TAG, "fillShortsBuff");
        short[] buffer = new short[audioBuffSizeShorts];
        int readCount = 0;
        int readOffset = 0;
        while (isStreaming() && isReadyToStream() && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        if (debug) Log.d(TAG, "fillShortsBuff done");
        return buffer;
    }

}
