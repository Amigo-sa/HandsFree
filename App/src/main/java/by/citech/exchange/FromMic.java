package by.citech.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Arrays;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class FromMic
        implements ITransmitterCtrl {

    private static final String TAG = Tags.FROM_MIC;
    private static final boolean debug = Settings.debug;
    private static final int bufferSize = Settings.bufferSize;

    private byte[] bytesBuffer;
    private short[] shortsBuffer;
    private AudioRecord recorder;
    private boolean isStreaming = false;
    private ITransmitter iTransmitter;

    public FromMic(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
        bytesBuffer = new byte[bufferSize];
        shortsBuffer = new short[bufferSize];
    }

    @Override
    public void prepareStream() {
        if (debug) Log.i(TAG, "prepareStream");
        if (debug) Log.i(TAG, String.format("prepareStream audioOutBuffersize is %d", bufferSize));
        streamOff();
        recorder = new AudioRecord(
                Settings.audioSource,
                Settings.audioRate,
                Settings.audioInChannel,
                Settings.audioEncoding,
                bufferSize);
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "run");
        recorder.startRecording();
        isStreaming = true;
        while (isStreaming) {
            if (debug) Log.i(TAG, String.format("run shortsBuffer length is %d", bufferSize));
            fillBuffer(shortsBuffer, bufferSize);
            if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(shortsBuffer)));
            iTransmitter.sendData(shortsBuffer);
        }
        if (debug) Log.w(TAG, "run done");
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
        while (isStreaming && (readLeft > 0)) {
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
        while (isStreaming && (readLeft > 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
        if (debug) Log.i(TAG, "fillBuffer done");
    }

}
