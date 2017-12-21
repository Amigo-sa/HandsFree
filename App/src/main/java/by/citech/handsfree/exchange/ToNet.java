package by.citech.handsfree.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.SeverityLevel;

public class ToNet
        implements ITransmitterCtrl, ISettingsCtrl, IPrepareObject {

    private static final String STAG = Tags.TO_NET;
    private final String TAG;
    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private boolean netSignificantAll;
    private int netSendSize;
    private int netChunkSignificantBytes;
    private int netChunkSize;
    private int netFactor;
    private byte[] netChunk;

    private static int objCount;
    private final int objNumber;

    static {
        objCount = 0;
    }

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
        netChunk = new byte[netChunkSize];
        return true;
    }

    //--------------------- non-settings

    private ITransmitter iTransmitter;
    private boolean isStreaming;
    private StorageData<byte[]> source;

    public ToNet(ITransmitter iTransmitter, StorageData<byte[]> source) {
        this.iTransmitter = iTransmitter;
        this.source = source;
    }

    public void prepareStream() {
        if (debug) Log.i(TAG, "prepareStream");
        prepareObject();
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "run");
        if (source == null) {
            Log.e(TAG, "streamOn source is null, return");
            return;
        }
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int netChunkCount = 0;
        Log.w(TAG, String.format(Locale.US, "streamOn parameters is:" +
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
        while (isStreaming) {
            while (source.isEmpty()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isStreaming) return;
            }
            netChunk = source.getData();
            if (netChunk != null && netChunk.length != 0) {
                if (netChunk.length != netChunkSize) {
                    Log.e(TAG, String.format(Locale.US, "streamOn readed chunk of length %d, expected %d", netChunk.length, netChunkSize));
                } else {
                    baos.write(netChunk, 0, netChunkSize);
                    netChunkCount++;
                }
            } else {
                Log.e(TAG, "streamOn readed invalid data from storage, netChunk is null or zero length");
            }
            if (!isStreaming) return;
            if (debug) Log.i(TAG, String.format("streamOn net out buff contains %d netChunks of %d bytes each", netChunkCount, netChunkSize));
            if (netChunkCount == netFactor) {
                if (debug) Log.w(TAG, String.format("streamOn net out buff contains enough data of %d bytes, sending", baos.size()));
                iTransmitter.sendData(baos.toByteArray());
                netChunkCount = 0;
                baos.reset();
            } else if (netChunkCount > netFactor) {
                Log.e(TAG, "streamOn too much data in net out buff");
                netChunkCount = 0;
                baos.reset();
            }
        }
        if (debug) Log.i(TAG, "streamOn done");
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        isStreaming = false;
        iTransmitter = null;
        source = null;
    }

}
