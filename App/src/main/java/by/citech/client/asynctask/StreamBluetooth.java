package by.citech.client.asynctask;

import android.util.Log;

import by.citech.client.network.IStream;
import by.citech.client.network.IClientCtrl;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class StreamBluetooth implements IStream {
    private byte[] buffer;
    private IClientCtrl iClientCtrl;
    private int bufferSize;
    private StorageData storageBtToNet;
    private boolean isStreaming = false;

    public StreamBluetooth(IClientCtrl iClientCtrl, int bufferSize, StorageData storageBtToNet) {
        this.iClientCtrl = iClientCtrl;
        this.bufferSize = bufferSize;
        this.storageBtToNet = storageBtToNet;
    }

    public IStream start() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_BLUETOOTH, "start");
        return this;
    }

    public void run() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_BLUETOOTH, "run");
        isStreaming = true;
        while (isStreaming) {
            buffer = storageBtToNet.getData();
            if (buffer.length > 0) {
                iClientCtrl.sendBytes(buffer);
            }
        }
        if (Settings.debug) Log.i(Tags.CLT_STREAM_BLUETOOTH, "run done");
    }

    @Override
    public void streamOff() {
        if (Settings.debug) Log.i(Tags.CLT_STREAM_BLUETOOTH, "streamOff");
        isStreaming = false;
        storageBtToNet.notify();
    }
}
