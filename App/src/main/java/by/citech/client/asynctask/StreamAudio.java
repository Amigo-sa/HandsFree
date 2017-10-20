package by.citech.client.asynctask;

import android.media.AudioRecord;
import android.util.Log;

import by.citech.client.network.IStream;
import by.citech.client.network.IClientCtrl;
import by.citech.connection.ITransmitter;
import by.citech.param.Settings;
import by.citech.param.Tags;
import static by.citech.util.Decode.bytesToHex;

public class StreamAudio implements IStream {
    private byte[] buffer;
    private int bufferSize;
    private AudioRecord recorder;
    private boolean isStreaming = false;
    private ITransmitter iTransmitter;

    public StreamAudio(ITransmitter iTransmitter, int bufferSize) {
        this.iTransmitter = iTransmitter;
        this.bufferSize = bufferSize;
    }

    @Override
    public void streamOff() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "streamOff");
        isStreaming = false;

        if (recorder != null) {
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                recorder.stop();
            }

            recorder.release();
            recorder = null;
        }
    }

    public IStream start() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "start");
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("start audioOutBuffersize is %d", bufferSize));
        streamOff();
        recorder = new AudioRecord(
                Settings.audioSource,
                Settings.audioRate,
                Settings.audioInChannel,
                Settings.audioEncoding,
                bufferSize);
        buffer = new byte[bufferSize];
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("start buffer length is %d", buffer.length));

        if (recorder == null) {
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "start recorder is null");
            return null;
        }

        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "start recorder started");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "run");
        recorder.startRecording();
        isStreaming = true;

        while (isStreaming) {
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("run buffer length is %d", buffer.length));
            fillBuffer(buffer, 0, buffer.length);
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("run %s: %s", "sendBytes", bytesToHex(buffer)));
            iTransmitter.sendBytes(buffer);
        }

        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "run done");
    }

    private void fillBuffer(byte[] buffer, int readOffset, int readLeft) {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "fillBuffer");
        int readCount;

        while (isStreaming && (readLeft > 0)) {
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("fillBuffer readLeft-1 is %d", readLeft));
            readCount = recorder.read(buffer, readOffset, readLeft);
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("fillBuffer readCount is %d", readCount));
            readLeft -= readCount;
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("fillBuffer readLeft-2 is %d", readLeft));
            readOffset += readCount;
            if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, String.format("fillBuffer readOffset is %d", readOffset));
        }

        if (Settings.debug) Log.i(Tags.CLT_STREAM_AUDIO, "fillBuffer done");
    }
}
