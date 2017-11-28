package by.citech.exchange;

import android.media.AudioRecord;
import android.util.Log;

import by.citech.param.Settings;
import by.citech.param.Tags;
import static by.citech.util.Decode.bytesToHexMark1;

public class FromMic implements ITransmitterCtrl {

    private static final String TAG = Tags.NET_STREAM_AUDIO;
    private static final boolean debug = Settings.debug;
    private static final int bufferSize = Settings.bufferSize;

    private byte[] buffer;
    private AudioRecord recorder;
    private boolean isStreaming = false;
    private ITransmitter iTransmitter;

    public FromMic(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
        buffer = new byte[bufferSize];
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
        if (debug) Log.i(TAG, String.format("prepare buffer length is %d", buffer.length));
        if (recorder == null) {
            Log.e(TAG, "prepare recorder is null");
        }
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        recorder.startRecording();
        isStreaming = true;
        while (isStreaming) {
            if (debug) Log.i(TAG, String.format("run buffer length is %d", buffer.length));
            fillBuffer(buffer, 0, buffer.length);
            if (debug) Log.i(TAG, String.format("startClient %s: %s", "sendData", bytesToHexMark1(buffer)));
            iTransmitter.sendData(buffer);
        }
        if (debug) Log.i(TAG, "run done");
    }

    private void fillBuffer(byte[] buffer, int readOffset, int readLeft) {
        if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
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
