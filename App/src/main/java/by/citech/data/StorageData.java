package by.citech.data;

import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

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
        Log.i(TAG, "isEmpty = " + databuffer.isEmpty());
        while (databuffer.isEmpty()) {
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

        while (databuffer.size() > Settings.storageMaxSize) {
            try {
                wait();
            } catch (InterruptedException e) {
                Log.i(TAG, "putData cant wait");
            }
        }

        final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
        for (byte byteChar : dataByte)
            stringBuilder.append(String.format("%02X ", byteChar));
        Log.i(TAG, "PUT DATA = " + stringBuilder.toString());

        databuffer.add(dataByte);
        notify();
        if (Settings.debug) Log.i(TAG, "putData done");
    }
}
