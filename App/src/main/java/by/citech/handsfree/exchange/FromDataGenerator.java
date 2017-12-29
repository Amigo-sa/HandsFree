package by.citech.handsfree.exchange;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.util.DataGenerator;

public class FromDataGenerator
        implements ITransmitterCtrl {

    private final String TAG = Tags.FromDataGenerator;
    private static final boolean debug = Settings.debug;

    private ITransmitter iTransmitter;
    private int chunkNumber;
    private int buffSize;
    private int buffPeriod;
    private short[] shortsBuffer;
    private byte[] bytesBuffer;
    private boolean isStreaming;
    private boolean isShorts;

    //--------------------- constructor

    public FromDataGenerator(ITransmitter iTransmitter, int buffSize, int buffPeriod, boolean isShorts) throws Exception {
        if (iTransmitter == null || buffSize < 1 || buffPeriod < 1) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.iTransmitter = iTransmitter;
        this.buffSize = buffSize;
        this.buffPeriod = buffPeriod;
        this.isShorts = isShorts;
        if (this.isShorts) {
            shortsBuffer = new short[this.buffSize];
        } else {
            bytesBuffer = new byte[this.buffSize];
        }
    }

    //--------------------- ITransmitterCtrl

    @Override
    public void prepareStream() {
        if (debug) Log.i(TAG, "prepareStream");
        if (debug) Log.w(TAG, String.format(Locale.US, "prepareStream parameters is:" +
                        " buffSize is %d," +
                        " isShorts is %b",
                buffSize,
                isShorts
        ));
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "streamOn");
        if (isStreaming) {
            if (debug) Log.w(TAG, "streamOn already streaming");
            return;
        }
        isStreaming = true;
        while (isStreaming) {
            streamShorts();
            try {
                Thread.sleep(buffPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        isStreaming = false;
    }

    //--------------------- main

    private void streamShorts() {
        shortsBuffer = DataGenerator.Sine.getSineChunk(chunkNumber);
        if (debug) Log.i(TAG, String.format(
                "streamShorts chunkNumber is %d, data is %s",
                chunkNumber, Arrays.toString(shortsBuffer))
        );
        iTransmitter.sendData(shortsBuffer);
        if (chunkNumber == 3) chunkNumber = 0;
        else                  chunkNumber++;
    }

}
