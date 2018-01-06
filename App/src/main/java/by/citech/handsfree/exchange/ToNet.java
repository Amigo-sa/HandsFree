package by.citech.handsfree.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class ToNet
        implements ITransmitterCtrl, ISettingsCtrl, IPrepareObject {

    private static final String STAG = Tags.TO_NET;
    private final String TAG;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final int objNumber;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private boolean netSignificantAll;
    private int netSendSize;
    private int netChunkSignificantBytes;
    private int netChunkSize;
    private int netFactor;
    private byte[] netChunk;
    private ITransmitter iTransmitter;
    private boolean isStreaming;
    private boolean isFinished;
    private boolean isPrepared;
    private StorageData<byte[]> source;

    {
        objCount++;
        objNumber = objCount;
        TAG = STAG + " " + objNumber;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        applySettings(null);
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return netChunk != null;
    }

    @Override
    public boolean takeSettings() {
        ISettingsCtrl.super.takeSettings();
        netSignificantAll = Settings.netSignificantAll;
        netChunkSignificantBytes = Settings.netChunkSignificantBytes;
        netChunkSize = netSignificantAll ? Settings.netChunkSize : netChunkSignificantBytes;
        netFactor = Settings.netFactor;
        netSendSize = netChunkSize * netFactor;
        return true;
    }

    @Override
    public boolean applySettings(SeverityLevel severityLevel) {
        ISettingsCtrl.super.applySettings(severityLevel);
        netChunk = new byte[netChunkSize];
        return true;
    }

    //--------------------- constructor

    ToNet(StorageData<byte[]> source) throws Exception {
        if (source == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        }
        this.source = source;
    }

    //--------------------- ITransmitterCtrl

    @Override
    public void prepareStream(ITransmitter iTransmitter) throws Exception {
        if (isFinished) {
            if (debug) Log.w(TAG, "prepareStream stream is finished, return");
            return;
        } else if (iTransmitter == null) {
            throw new Exception(TAG + " " + StatusMessages.ERR_PARAMETERS);
        } else {
            if (debug) Log.i(TAG, "prepareStream");
            this.iTransmitter = iTransmitter;
        }
        if (debug) Log.w(TAG, String.format(Locale.US, "streamOn parameters is:" +
                        " netSignificantAll is %b," +
                        " netChunkSignificantBytes is %d," +
                        " netChunkSize is %d," +
                        " netFactor is %d," +
                        " netSendSize is %d",
                netSignificantAll,
                netChunkSignificantBytes,
                netChunkSize,
                netFactor,
                netSendSize
        ));
        isPrepared = true;
    }

    @Override
    public void finishStream() {
        if (debug) Log.i(TAG, "finishStream");
        isFinished = true;
        streamOff();
        iTransmitter = null;
        source = null;
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

    @Override
    public void streamOn() {
        if (isStreaming() || !isReadyToStream()) {
            return;
        } else {
            if (debug) Log.i(TAG, "streamOn");
        }
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int netChunkSizeActual = 0;
        int netChunkCount = 0;
        while (isStreaming()) {
            while (isStreaming() && isReadyToStream() && source.isEmpty()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isStreaming() || !isReadyToStream()) return;
            netChunk = source.getData();
            if (netChunk != null) {
                netChunkSizeActual = netChunk.length;
                if (netChunkSizeActual != netChunkSize) {
                    if (debug) Log.e(TAG, String.format(Locale.US, "streamOn readed chunk of length %d, expected %d", netChunkSizeActual, netChunkSize));
                } else {
                    baos.write(netChunk, 0, netChunkSize);
                    netChunkCount++;
                }
            } else {
                if (debug) Log.e(TAG, "streamOn readed null data from storage");
                return;
            }
            if (debug) Log.i(TAG, String.format("streamOn net out buff contains %d netChunks of %d bytes each", netChunkCount, netChunkSize));
            if (netChunkCount == netFactor) {
                if (debug) Log.w(TAG, String.format("streamOn net out buff contains enough data of %d bytes, sending", baos.size()));
                if (isStreaming() && isReadyToStream()) {
                    iTransmitter.sendData(baos.toByteArray());
                }
                netChunkCount = 0;
                baos.reset();
            } else if (netChunkCount > netFactor) {
                if (debug) Log.e(TAG, "streamOn too much data in net out buff");
                netChunkCount = 0;
                baos.reset();
            }
        }
        if (debug) Log.i(TAG, "streamOn done");
    }

}
