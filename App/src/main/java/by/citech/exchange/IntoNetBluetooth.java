package by.citech.exchange;

import android.util.Log;

import java.io.ByteArrayOutputStream;

import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.Tags;

class IntoNetBluetooth
        implements IIntoNetCtrl {

    private static final String TAG = Tags.NET_STREAM_BT;
    private static final boolean debug = Settings.debug;

    private ITransmitter iTransmitter;
    private boolean isStreaming = false;
    private StorageData<byte[]> storageBtToNet;

    public IntoNetBluetooth(ITransmitter iTransmitter, StorageData<byte[]> storageBtToNet) {
        this.iTransmitter = iTransmitter;
        this.storageBtToNet = storageBtToNet;
    }

    public IIntoNetCtrl start() {
        if (debug) Log.i(TAG, "start");
        return this;
    }

    public void run() {
        if (debug) Log.i(TAG, "run");
        isStreaming = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferedDataSize;
        //TODO: упростить логику
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
            if (bufferedDataSize == Settings.btToNetSendSize) {
                if (debug) Log.i(TAG, "run network output buffer contains enough data, sending");
                iTransmitter.sendData(baos.toByteArray());
                baos.reset();
            } else if (bufferedDataSize > Settings.btToNetSendSize) {
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
