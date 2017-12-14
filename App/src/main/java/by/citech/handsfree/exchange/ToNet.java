package by.citech.handsfree.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.param.Settings;
import by.citech.handsfree.param.Tags;

public class ToNet
        implements ITransmitterCtrl {

    private static final String TAG = Tags.TO_NET;
    private static final boolean debug = Settings.debug;

    //--------------------- settings

    private int dataChunkSize;
    private int netSendSize;
    private int netChunkSignificantBytes;
    private byte[] dataChunk;

    {
        takeSettings();
        applySettings();
    }

    private void takeSettings() {
        dataChunkSize = Settings.bt2btPacketSize;
        netSendSize = Settings.netSendSize;
        netChunkSignificantBytes = Settings.netChunkSignificantBytes;
    }

    private void applySettings() {
        dataChunk = new byte[dataChunkSize];
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
        int bufferedDataSize;
        //TODO: упростить логику
        while (isStreaming) {
            while (source.isEmpty()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isStreaming) return;
            }
            dataChunk = source.getData();
            if (dataChunk != null && dataChunk.length != 0) {
                baos.write(dataChunk, 0, netChunkSignificantBytes);
            } else {
                Log.e(TAG, "readed null from storage");
            }
            if (!isStreaming) return;
            bufferedDataSize = baos.size();
            if (debug) Log.i(TAG, String.format("run network output buffer contains %d bytes", bufferedDataSize));
            //TODO: добавить логику обрезки на случай вычитки большего количества данных
            if (bufferedDataSize == netSendSize) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                iTransmitter.sendData(baos.toByteArray());
                baos.reset();
            } else if (bufferedDataSize > netSendSize) {
                Log.e(TAG, "run too much data in network output buffer");
                baos.reset();
            }
        }
        if (debug) Log.i(TAG, "run done");
    }

    @Override
    public void streamOff() {
        if (debug) Log.i(TAG, "streamOff");
        isStreaming = false;
    }

}
