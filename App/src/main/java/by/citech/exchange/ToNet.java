package by.citech.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ToNet
        implements ITransmitterCtrl {

    private static final String TAG = Tags.NET_TRANSMIT;
    private static final boolean debug = Settings.debug;
    private static final int dataChunkSize = Settings.btToBtSendSize;

    private ITransmitter iTransmitter;
    private boolean isStreaming = false;
    private StorageData<byte[]> source;
    private byte[] dataChunk;

    public ToNet(ITransmitter iTransmitter, StorageData<byte[]> source) {
        this.iTransmitter = iTransmitter;
        this.source = source;
        dataChunk = new byte[dataChunkSize];
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
                baos.write(dataChunk, 0, Settings.btSignificantBytes);
            } else {
                Log.e(TAG, "readed null from storage");
            }
            if (!isStreaming) return;
            bufferedDataSize = baos.size();
            if (debug) Log.i(TAG, String.format("run network output buffer contains %d bytes", bufferedDataSize));
            //TODO: добавить логику обрезки на случай вычитки большего количества данных
            if (bufferedDataSize == Settings.toBtSendSize) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                iTransmitter.sendData(baos.toByteArray());
                baos.reset();
            } else if (bufferedDataSize > Settings.toBtSendSize) {
                if (debug) Log.e(TAG, "run too much data in network output buffer");
                return;
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
