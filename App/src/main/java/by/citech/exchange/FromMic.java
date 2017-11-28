package by.citech.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Arrays;

import by.citech.param.Settings;
import by.citech.param.Tags;

public class FromMic implements ITransmitterCtrl {

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

    public void prepare() {
        if (debug) Log.i(TAG, "prepare");
        if (debug) Log.i(TAG, String.format("prepare audioOutBuffersize is %d", bufferSize));
        streamOff();
        recorder = new AudioRecord(
                Settings.audioSource,
                Settings.audioRate,
                Settings.audioInChannel,
                Settings.audioEncoding,
                bufferSize);
        if (debug) Log.i(TAG, String.format("prepare shortsBuffer length is %d", shortsBuffer.length));
        if (recorder == null) {
            Log.e(TAG, "prepare recorder is null");
        }
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        recorder.startRecording();
        isStreaming = true;

        while (isStreaming) {
            if (debug) Log.i(TAG, String.format("run shortsBuffer length is %d", shortsBuffer.length));
            fillBuffer(shortsBuffer, shortsBuffer.length);
//          if (debug) Log.i(TAG, String.format("run sendData: %s", bytesToHexMark1(shortsBuffer)));
            if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(shortsBuffer)));
            iTransmitter.sendData(shortsBuffer);
        }
        if (debug) Log.w(TAG, "run done");
    }

    private void fillBuffer(byte[] buffer, int readLeft) {
        if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;

        while (isStreaming && (readLeft > 0)) {
            if (debug) Log.i(TAG, String.format("fillBuffer readLeft-1 is %d", readLeft));
            readCount = recorder.read(buffer, readOffset, readLeft);
            if (debug) Log.i(TAG, String.format("fillBuffer readCount is %d", readCount));
            readLeft -= readCount;
            if (debug) Log.i(TAG, String.format("fillBuffer readLeft-2 is %d", readLeft));
            readOffset += readCount;
            if (debug) Log.i(TAG, String.format("fillBuffer readOffset is %d", readOffset));
        }
        if (debug) Log.i(TAG, "fillBuffer done");
    }

    private void fillBuffer(short[] buffer, int readLeft) {
        if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;

        while (isStreaming && (readLeft > 0)) {
            if (debug) Log.i(TAG, String.format("fillBuffer readLeft-1 is %d", readLeft));
            readCount = recorder.read(buffer, readOffset, readLeft);
            if (debug) Log.i(TAG, String.format("fillBuffer readCount is %d", readCount));
            readLeft -= readCount;
            if (debug) Log.i(TAG, String.format("fillBuffer readLeft-2 is %d", readLeft));
            readOffset += readCount;
            if (debug) Log.i(TAG, String.format("fillBuffer readOffset is %d", readOffset));
        }
        if (debug) Log.i(TAG, "fillBuffer done");
    }

}
