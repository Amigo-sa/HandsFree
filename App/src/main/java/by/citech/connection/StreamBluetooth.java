package by.citech.connection;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class StreamBluetooth implements IStream {
    private byte[] buffer;
    private ITransmitter iTransmitter;
    private int bufferSize;
    private StorageData storageBtToNet;
    private boolean isStreaming = false;

    public StreamBluetooth(ITransmitter iTransmitter, int bufferSize, StorageData storageBtToNet) {
        this.iTransmitter = iTransmitter;
        this.bufferSize = bufferSize;
        this.storageBtToNet = storageBtToNet;
    }

    public IStream start() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "start");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run");
        isStreaming = true;
        while (isStreaming) {
            buffer = storageBtToNet.getData();
            if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run storageBtToNet.getData()");
            if (buffer.length > 0) {
                if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run buffer.length > 0");
                iTransmitter.sendBytes(buffer);
                if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run sendBytes(buffer)");
            }
        }
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run done");
    }

    @Override
    public void streamOff() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "streamOff");
        isStreaming = false;
        storageBtToNet.notify();
    }
}
