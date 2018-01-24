package by.citech.handsfree.exchange.producers;

import android.util.Log;

import java.util.Locale;

import by.citech.handsfree.exchange.IRxComplex;
import by.citech.handsfree.exchange.IStreamer;
import by.citech.handsfree.generator.DataGeneratorFactory;
import by.citech.handsfree.generator.EDataType;
import by.citech.handsfree.generator.IDataGenerator;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

public class FromGenerator
        implements IStreamer {

    private final String TAG = Tags.FromDataGenerator;
    private static final boolean debug = Settings.debug;

    private IRxComplex iRxComplex;
    private IDataGenerator dataGenerator;
    private EDataType dataType;
    private int buffSize;
    private int idleInterval;
    private boolean isStreaming;
    private boolean isPrepared;
    private boolean isFinished;
    private boolean isShorts;

    //--------------------- constructor

    public FromGenerator(int buffSize, int idleInterval, boolean isShorts, EDataType dataType) throws Exception {
        if (buffSize < 1 || idleInterval < 1) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.buffSize = buffSize;
        this.idleInterval = idleInterval;
        this.isShorts = isShorts;
        this.dataType = dataType;
    }

    //--------------------- IStreamer

    @Override
    public void prepareStream(IRxComplex receiver) throws Exception {
        if (isFinished) {
            if (debug) Log.w(TAG, "prepareStream stream is finished, return");
            return;
        } else if (receiver == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        } else {
            if (debug) Log.i(TAG, "prepareStream");
            this.iRxComplex = receiver;
        }
        dataGenerator = DataGeneratorFactory.getDataGenerator(buffSize, isShorts, dataType);
        if (debug) Log.w(TAG, String.format(Locale.US, "prepareStream parameters is:" +
                        " buffSize is %d," +
                        " isShorts is %b",
                buffSize,
                isShorts
        ));
        if (dataGenerator != null) {
            isPrepared = true;
        }
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        isFinished = true;
        streamOff();
        iRxComplex = null;
    }

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            if (debug) Log.i(TAG, "streamOn");
        }
        isStreaming = true;
        while (isStreaming() && isReadyToStream()) {
            if (isShorts) {
                streamShorts();
            } else {
                streamBytes();
            }
            try {
                Thread.sleep(idleInterval);
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

    //--------------------- main

    private void streamShorts() {
        iRxComplex.sendData(dataGenerator.getNextDataShorts());
    }

    private void streamBytes() {
        iRxComplex.sendData(dataGenerator.getNextDataBytes());
    }

}
