package by.citech.data;

import android.util.Log;
import android.util.Xml;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import by.citech.BuildConfig;
import by.citech.param.Settings;
import by.citech.param.Tags;

import static by.citech.util.Decode.bytesToHexMark1;

public class StorageData {
    private String TAG;
    private Deque<byte[]> databuffer;
   /* // TODO: попробовать перейти на Queue<>
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
        }

        databuffer.add(dataByte);
        notify();
        if (Settings.debug) Log.i(TAG, "putData done");
    }*/

    public StorageData(String TAG) {
        this.TAG = TAG;
        databuffer = new ArrayDeque<>();
    }

    public boolean isEmpty(){
        if (Settings.debug) Log.i(TAG, "isEmpty");
        return (databuffer.size() == 0);
    }

//    public static byte[] concatByteArrays(byte[]... inputs) { //TODO доделать для больших пакетов
//        int i = 0;
//        for (byte[] b : inputs) {
//            i += b.length;
//        }
//        byte[] r = new byte[i];
//        i = 0;
//        for (byte[] b : inputs) {
//            System.arraycopy(b, 0, r, i, b.length);
//            i += b.length;
//        }
//        return r;
//    }

    public byte[] getData() {
        if (Settings.debug) Log.i(TAG, "getData");
        return databuffer.poll();
    }

    public void putData(byte[] dataByte) {
        if (Settings.debug) Log.i(TAG, "putData");
        databuffer.push(dataByte);
    }














}
