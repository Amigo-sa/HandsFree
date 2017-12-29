package by.citech.handsfree.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;
import by.citech.handsfree.util.DataGenerator;

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
        bytesBuffer = new byte[audioBuffSizeBytes];
        shortsBuffer = new short[audioBuffSizeShorts];
        return true;
    }

    //--------------------- constructor

    public FromAudioIn(ITransmitter iTransmitter) throws Exception {
        if (iTransmitter == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.iTransmitter = iTransmitter;
    }

    //--------------------- ITransmitterCtrl

    @Override
    public void prepareStream() {
        if (debug) Log.i(TAG, "prepareStream");
        streamOff();
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
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "streamOn");
        if (isStreaming || (recorder == null)) {
            if (debug) Log.w(TAG, "streamOn already streaming or recorder is null");
            return;
        }
        isStreaming = true;
        recorder.startRecording();
        while (isStreaming) {
            if (audioBuffIsShorts) streamShorts();
            else                   streamBytes();
        }
        if (debug) Log.w(TAG, "streamOn done");
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        isStreaming = false;
        if (recorder != null) {
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                recorder.stop();
            }
            recorder.release();
            recorder = null;
        }
    }

    //--------------------- main

    private void streamShorts() {
        fillBuffer(shortsBuffer, audioBuffSizeShorts);
        iTransmitter.sendData(shortsBuffer);
    }

    private void streamBytes() {
        fillBuffer(bytesBuffer, audioBuffSizeBytes);
        iTransmitter.sendData(bytesBuffer);
    }

    private void fillBuffer(byte[] buffer, int readLeft) {
//      if (debug) Log.i(TAG, "fillBuffer byte[]");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
//      if (debug) Log.i(TAG, "fillBuffer byte[] done");
    }

    private void fillBuffer(short[] buffer, int readLeft) {
//      if (debug) Log.i(TAG, "fillBuffer short[]");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
//          if (debug) Log.i(TAG, "fillBuffer readLeft is " + readLeft);
            readCount = recorder.read(buffer, readOffset, readLeft);
//          if (debug) Log.i(TAG, "fillBuffer short[] readCount is " + readCount);
            readLeft -= readCount;
//          if (debug) Log.i(TAG, "fillBuffer short[] readCount is " + readCount);
            readOffset += readCount;
//          if (debug) Log.i(TAG, "fillBuffer short[] readOffset is " + readOffset);
        }
//      if (debug) Log.i(TAG, "fillBuffer short[] done");
    }

}
