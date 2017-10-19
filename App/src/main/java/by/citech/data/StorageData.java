package by.citech.data;

import android.util.Log;
import java.util.ArrayList;

import by.citech.param.Settings;

public class StorageData {
    private String TAG;
    private ArrayList<byte[]> databuffer;

    public StorageData(String TAG) {
        this.TAG = TAG;
        databuffer = new ArrayList<byte[]>();
    }

    public synchronized byte[] getData() {
        if (Settings.debug) Log.i(TAG, "getData");
        byte[] tmpData;

        if (databuffer.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Log.i(TAG, "getData cant wait");
            }
        }

        tmpData = databuffer.get(0);
        databuffer.remove(0);
        notify();
        if (Settings.debug) Log.i(TAG, "getData done");
        return tmpData;
    }

    public synchronized void putData(byte[] dataByte) {
        if (Settings.debug) Log.i(TAG, "putData");

        if (databuffer.size() > Settings.storageMaxSize) {
            try {
                wait();
            } catch (InterruptedException e) {
                Log.i(TAG, "putData cant wait");
            }
        }

        databuffer.add(dataByte);
        notify();
        if (Settings.debug) Log.i(TAG, "putData done");
    }
}
