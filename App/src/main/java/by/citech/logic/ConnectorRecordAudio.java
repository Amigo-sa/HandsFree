package by.citech.logic;

import android.util.Log;

import by.citech.data.StorageData;
import by.citech.exchange.FromMic;
import by.citech.exchange.ITransmitter;
import by.citech.exchange.ITransmitterCtrl;
import by.citech.param.Settings;
import by.citech.param.Tags;

public class ConnectorRecordAudio implements ITransmitter {

    private static final String TAG = Tags.AUDREC_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private ITransmitterCtrl iTransmitterCtrl;
    private StorageData<byte[]> storage;

    //--------------------- singleton

    private static volatile ConnectorRecordAudio instance = null;

    private ConnectorRecordAudio() {
    }

    public static ConnectorRecordAudio getInstance() {
        if (instance == null) {
            synchronized (ConnectorRecordAudio.class) {
                if (instance == null) {
                    instance = new ConnectorRecordAudio();
                }
            }
        }
        return instance;
    }

    //--------------------- getters and setters

    public void setStorage(StorageData<byte[]> storage) {
        this.storage = storage;
    }

    public void start() {
        if (debug) Log.i(TAG, "prepare");
        if (storage == null) {
            Log.e(TAG, "prepare illegal parameters");
            return;
        }
        FromMic fromMic = new FromMic(this);
        fromMic.prepare();
        iTransmitterCtrl = fromMic;
        new Thread(fromMic::run).start();
    }

    public void stop() {
        if (debug) Log.i(TAG, "stop");
        if (iTransmitterCtrl != null){
            iTransmitterCtrl.streamOff();
            iTransmitterCtrl = null;
        }
        if (storage != null) {
            storage.clear();
            storage = null;
        }
    }

    @Override
    public void sendMessage(String message) {
        Log.e(TAG, "sendMessage excess method");
    }

    @Override
    public void sendData(byte[] data) {
        if (debug) Log.i(TAG, "sendData");
        storage.putData(data);
    }
}
