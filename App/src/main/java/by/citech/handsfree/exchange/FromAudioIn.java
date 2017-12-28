package by.citech.handsfree.exchange;

import android.media.AudioRecord;
import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.util.DataGenerator;

public class FromAudioIn
        implements ITransmitterCtrl {

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

    {
        initiate();
    }

    private void initiate() {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        audioBuffIsShorts = Settings.audioBuffIsShorts;
        audioSource = Settings.audioSource;
        audioRate = Settings.audioRate;
        audioInChannel = Settings.audioInChannel;
        audioEncoding = Settings.audioEncoding;
        audioBuffSizeBytes = Settings.audioSingleFrame
                ? (Settings.audioCodecType.getDecodedShortsSize() * 2)
                : Settings.audioBuffSizeBytes;
        audioBuffSizeShorts = audioBuffSizeBytes / 2;
    }

    private void applySettings() {
        bytesBuffer = new byte[audioBuffSizeBytes];
        shortsBuffer = new short[audioBuffSizeShorts];
    }

    //--------------------- non-settings

    private AudioRecord recorder;
    private boolean isStreaming;
    private ITransmitter iTransmitter;

    public FromAudioIn(ITransmitter iTransmitter) {
        this.iTransmitter = iTransmitter;
    }

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
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "streamOn");
        if (isStreaming || (recorder == null)) {
            Log.e(TAG, "streamOn already streaming or recorder is null");
            return;
        }
        Log.w(TAG, String.format(Locale.US, "streamOn parameters is:" +
                        " audioBuffIsShorts is %b," +
                        " audioRate is %d," +
                        " audioBuffSizeBytes is %d," +
                        " audioBuffSizeShorts is %d",
                audioBuffIsShorts,
                audioRate,
                audioBuffSizeBytes,
                audioBuffSizeShorts
        ));
        isStreaming = true;
//      recorder.startRecording();
        while (isStreaming) {
            if (audioBuffIsShorts) {
                streamShorts();
            } else {
                streamBytes();
            }
        }
        if (debug) Log.w(TAG, "streamOn done");
    }

    private int chunkNumber;

    private void streamShorts() {
//      fillBuffer(shortsBuffer, audioBuffSizeShorts);
//      if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(shortsBuffer)));
//      if (debug) Log.i(TAG, String.format("streamShorts size: %s", shortsBuffer.length));
//      iTransmitter.sendData(shortsBuffer);
        shortsBuffer = DataGenerator.getSinusChunk(chunkNumber);
        if (debug) Log.i(TAG, String.format(
                "streamShorts chunkNumber is %d, data is %s",
                chunkNumber, Arrays.toString(shortsBuffer))
        );
        iTransmitter.sendData(shortsBuffer);
        if (chunkNumber == 3) chunkNumber = 0;
        else                  chunkNumber++;
    }

    private void streamBytes() {
        fillBuffer(bytesBuffer, audioBuffSizeBytes);
//      if (debug) Log.i(TAG, String.format("run sendData: %s", Arrays.toString(bytesBuffer)));
//      if (debug) Log.i(TAG, String.format("streamBytes size: %s", shortsBuffer.length));
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
//      if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
            readCount = recorder.read(buffer, readOffset, readLeft);
            readLeft -= readCount;
            readOffset += readCount;
        }
//      if (debug) Log.i(TAG, "fillBuffer done");
    }

    private void fillBuffer(short[] buffer, int readLeft) {
//      if (debug) Log.i(TAG, "fillBuffer");
        int readCount;
        int readOffset = 0;
        while (isStreaming && (readLeft != 0)) {
//          if (debug) Log.i(TAG, "fillBuffer readLeft is " + readLeft);
            readCount = recorder.read(buffer, readOffset, readLeft);
//          if (debug) Log.i(TAG, "fillBuffer readCount is " + readCount);
            readLeft -= readCount;
//          if (debug) Log.i(TAG, "fillBuffer readCount is " + readCount);
            readOffset += readCount;
//          if (debug) Log.i(TAG, "fillBuffer readOffset is " + readOffset);
        }
//      if (debug) Log.i(TAG, "fillBuffer done");
    }

}
