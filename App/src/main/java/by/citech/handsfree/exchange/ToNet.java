package by.citech.handsfree.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class ToNet
        implements ITransmitterCtrl {

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
        initiate();
    }

    private void initiate() {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        netSignificantAll = Settings.netSignificantAll;
        netChunkSignificantBytes = Settings.netChunkSignificantBytes;
        netChunkSize = netSignificantAll ? Settings.netChunkSize : netChunkSignificantBytes;
        netFactor = Settings.netFactor;
        netSendSize = netChunkSize * netFactor;
    }

    private void applySettings() {
        netChunk = new byte[netChunkSize];
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
    }

    @Override
    public void streamOn() {
        if (debug) Log.i(TAG, "run");
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      int bufferedDataSize;
        int netChunkCount = 0;
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
                baos.write(netChunk, 0, netChunkSize);
                netChunkCount++;
            } else {
                Log.e(TAG, "streamOn readed invalid data from storage");
            }
            if (!isStreaming) return;
//          bufferedDataSize = baos.size();
//          if (debug) Log.i(TAG, String.format("streamOn net out buff contains %d bytes", bufferedDataSize));
//          if (bufferedDataSize == netSendSize) {
//              if (debug) Log.i(TAG, "streamOn net out buff contains enough data, sending");
//              iTransmitter.sendData(baos.toByteArray());
//          } else if (bufferedDataSize > netSendSize) {
//              Log.e(TAG, "streamOn too much data in net out buff");
//          }
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
