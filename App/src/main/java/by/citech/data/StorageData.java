package by.citech.data;

import android.util.Log;
import android.util.Xml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import by.citech.BuildConfig;
import by.citech.param.Settings;

import static by.citech.util.Decode.bytesToHexMark1;

public class StorageData {
    private String TAG;
    private ArrayList<byte[]> databuffer;

    public StorageData(String TAG) {
        this.TAG = TAG;
        databuffer = new ArrayList<>();
    }

    public synchronized byte[] getData() {
        if (Settings.debug) Log.i(TAG, "getData");
        byte[] tmpData;
        if (Settings.debug) Log.i(TAG, "isEmpty = " + databuffer.isEmpty());

        while (databuffer.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            if (Settings.debug) Log.e(TAG, "putData overflow");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (Settings.debug) {
            final StringBuilder stringBuilder = new StringBuilder(dataByte.length);
            for (byte byteChar : dataByte)
                stringBuilder.append(String.format("%02X ", byteChar));
            Log.i(TAG, "PUT DATA = " + stringBuilder.toString());
            Log.i(TAG, "PUT DATA = " + bytesToHexMark1(dataByte));
            Log.i(TAG, "PUT DATA = " + new String(dataByte));
            Log.i(TAG, "PUT DATA = " + new String(dataByte, Charset.defaultCharset()));
            try {
                Log.i(TAG, "PUT DATA = " + new String(dataByte, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        databuffer.add(dataByte);
        notify();
        if (Settings.debug) Log.i(TAG, "putData done");
    }
}
