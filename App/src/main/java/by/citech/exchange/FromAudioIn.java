package by.citech.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Arrays;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class FromAudioIn
        implements ITransmitterCtrl {

    private final String TAG;
    private final boolean debug;

    private final boolean audioBuffIsShorts;
    private final int audioSource;
    private final int audioRate;
    private final int audioInChannel;
    private final int audioEncoding;
    private final int audioBuffSizeBytes;
    private final int audioBuffSizeShorts;

    {
        TAG = Tags.FROM_AUDIN;
        debug = Settings.debug;

        audioBuffIsShorts = Settings.audioBuffIsShorts;
        audioSource = Settings.audioSource;
        audioRate = Settings.audioRate;
        audioInChannel = Settings.audioInChannel;
        audioEncoding = Settings.audioEncoding;
        audioBuffSizeBytes = Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
    }

    private byte[] bytesBuffer;
    private short[] shortsBuffer;
    private AudioRecord recorder;
    private boolean isStreaming;
    private ITransmitter iTransmitter;

    public FromAudioIn(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
        bytesBuffer = new byte[audioBuffSizeBytes];
        shortsBuffer = new short[audioBuffSizeShorts];
    }

    @Override
    public void prepareStream() {
        if (debug) Log.i(TAG, "prepareStream");
        if (debug) Log.i(TAG, String.format("prepareStream audioOutBuffersize is %d", audioBuffSizeBytes));
        streamOff();
        recorder = new AudioRecord(
                audioSource,
                audioRate,
                audioInChannel,
                audioEncoding,
                audioBuffSizeBytes);
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "run");
        if (isStreaming) return;
        recorder.startRecording();
        isStreaming = true;
        while (isStreaming) {
            if (audioBuffIsShorts) {
                streamShorts();
            } else {
                streamBytes();
            }
        }
        if (debug) Log.w(TAG, "run done");
    }

    private void streamShorts() {
        fillBuffer(shortsBuffer, audioBuffSizeShorts);
        if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(shortsBuffer)));
        iTransmitter.sendData(shortsBuffer);
    }

    private void streamBytes() {
        fillBuffer(bytesBuffer, audioBuffSizeBytes);
        if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(bytesBuffer)));
        iTransmitter.sendData(bytesBuffer);
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

    private void fillBuffer(byte[] buffer, int readLeft) {
        if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        if (debug) Log.i(TAG, "fillBuffer done");
    }

    private void fillBuffer(short[] buffer, int readLeft) {
        if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
            if (debug) Log.i(TAG, "fillBuffer readLeft is " + readLeft);
            readCount = recorder.read(buffer, readOffset, readLeft);
            if (debug) Log.i(TAG, "fillBuffer readCount is " + readCount);
            readLeft -= readCount;
            if (debug) Log.i(TAG, "fillBuffer readCount is " + readCount);
            readOffset += readCount;
            if (debug) Log.i(TAG, "fillBuffer readOffset is " + readOffset);
        }
        if (debug) Log.i(TAG, "fillBuffer done");
    }

}
