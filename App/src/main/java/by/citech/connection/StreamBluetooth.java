package by.citech.connection;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class StreamBluetooth implements IStreamCtrl {
//  private byte[] buffer;
    private int bufferSize;
    private ITransmitter iTransmitter;
    private boolean isStreaming = false;
    private final StorageData storageBtToNet;

    public StreamBluetooth(ITransmitter iTransmitter, int bufferSize, StorageData storageBtToNet) {
        this.iTransmitter = iTransmitter;
        this.bufferSize = bufferSize;
        this.storageBtToNet = storageBtToNet;
    }

    public IStreamCtrl start() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "start");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run");
        isStreaming = true;
        while (isStreaming) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(storageBtToNet.getData());
                if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run baos.write(storageBtToNet.getData())");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!isStreaming) {
                baos.reset();
                return;
            }
            if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, String.format("baos.size() is %d",baos.size()));
            if (baos.size() > Settings.minNetSendSize) {
                if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run baos.size() > bufferSize");
                iTransmitter.sendBytes(baos.toByteArray());
                baos.reset();
            }
        }
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "run done");
    }

    @Override
    public void streamOff() {
        if (Settings.debug) Log.i(Tags.NET_STREAM_BLUETOOTH, "streamOff");
        isStreaming = false;
        synchronized (storageBtToNet) {
            storageBtToNet.notify();
        }
    }
}
