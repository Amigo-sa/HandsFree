package by.citech.handsfree.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import by.citech.handsfree.param.Tags;

public class Audio {

    private AudioRecord findAudioRecord(boolean debug) {
        int[] mSampleRates = new int[] {
                8000,
                11025,
                22050,
                32000,
                44100 };
        short[] mAudioFormatChannels = new short[] {
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.CHANNEL_IN_STEREO };
        final short[] mAudioFormatEncodings = new short[] {
                AudioFormat.ENCODING_PCM_8BIT,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.ENCODING_PCM_FLOAT };
        if (debug) Log.i(Tags.FROM_AUDIN, "findAudioRecord");
        int bufferLenghtActual;
        AudioRecord recorder = null;
        byte[] buffer;
        for (int rate : mSampleRates) {
            for (short encoding : mAudioFormatEncodings) {
                for (short channel : mAudioFormatChannels) {
                    try {
                        int bufferSizeMinimal = AudioRecord.getMinBufferSize(rate, channel, encoding);
                        if (debug) Log.i(Tags.FROM_AUDIN,
                                "SampleRate: "          + rate              + ". " +
                                        "Encoding: "            + encoding          + ". " +
                                        "Channels: "            + channel           + ". " +
                                        "Minimal buffer size: " + bufferSizeMinimal + ".");
                        if (bufferSizeMinimal != AudioRecord.ERROR_BAD_VALUE) {
                            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channel, encoding, bufferSizeMinimal * 10);
                            if (debug) Log.i(Tags.FROM_AUDIN, "findAudioRecord new AudioRecord");
                            Thread.sleep(100);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                Log.i(Tags.FROM_AUDIN, "findAudioRecord recorder is initialized");
                                bufferLenghtActual = bufferSizeMinimal * 10;
                                buffer = new byte[bufferLenghtActual];
                                return recorder;
                            }
                            if (debug) Log.i(Tags.FROM_AUDIN, "findAudioRecord recorder is not initialized, release");
                            recorder.release();
                            recorder = null;
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        if (debug) Log.i(Tags.FROM_AUDIN, "findAudioRecord Exception");
                        if (recorder != null) {
                            recorder.release();
                        }
                        recorder = null;
                    }
                }
            }
        }
        return null;
    }

}
