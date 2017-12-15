package by.citech.handsfree.logic;

import android.util.Log;

import by.citech.handsfree.data.StorageData;
import by.citech.handsfree.exchange.FromAudioIn;
import by.citech.handsfree.exchange.ITransmitter;
import by.citech.handsfree.exchange.ITransmitterCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class ConnectorRecordAudio
        implements ITransmitter {

    private static final String TAG = Tags.AUDREC_CONNECTOR;
    private static final boolean debug = Settings.debug;

    private ITransmitterCtrl iTransmitterCtrl;
    private StorageData<short[]> storage;

    public ConnectorRecordAudio(StorageData<short[]> storage) {
        this.storage = storage;
    }

    //--------------------- main

    public void start() {
        if (debug) Log.i(TAG, "prepareStream");
        if (storage == null) {
            Log.e(TAG, "prepareStream illegal parameters");
            return;
        }
        iTransmitterCtrl = new FromAudioIn(this);
        iTransmitterCtrl.prepareStream();
        new Thread(iTransmitterCtrl::streamOn).start();
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
        Log.e(TAG, "sendMessage String");
    }

    @Override
    public void sendData(byte[] data) {
        Log.e(TAG, "sendData byte[]");
    }

    @Override
    public void sendData(short[] data) {
        if (debug) Log.i(TAG, "sendData short[]");
        storage.putData(data);
    }

}
