package by.citech.network.control.transmit;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class StreamBluetooth implements IStreamCtrl {
    private static final String TAG = Tags.NET_STREAM_BLUETOOTH;
    private static final boolean debug = Settings.debug;
    private ITransmitter iTransmitter;
    private boolean isStreaming = false;
    private StorageData<byte[]> storageBtToNet;

    public StreamBluetooth(ITransmitter iTransmitter, StorageData<byte[]> storageBtToNet) {
        this.iTransmitter = iTransmitter;
        this.storageBtToNet = storageBtToNet;
    }

    public IStreamCtrl start() {
        if (debug) Log.i(TAG, "start");
        return this;
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferedDataSize;
        while (isStreaming) {
            while (storageBtToNet.isEmpty()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isStreaming) return;
            }
            baos.write(storageBtToNet.getData(), 0, Settings.btSignificantBytes);
            if (debug) Log.i(TAG, "run got data from storage");
            if (!isStreaming) return;
            bufferedDataSize = baos.size();
            if (debug) Log.i(TAG, String.format("run network output buffer contains %d bytes", bufferedDataSize));
            //TODO: добавить логику обрезки на случай вычитки большего количества данных
            if (bufferedDataSize == Settings.btnNetToNetSendSize) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                iTransmitter.sendData(baos.toByteArray());
                baos.reset();
            } else if (bufferedDataSize > Settings.btnNetToNetSendSize) {
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
