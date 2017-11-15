package by.citech.data;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;

import by.citech.param.Settings;

public class StorageData {
    private static final boolean debug = Settings.debug;
    private boolean debugGetSession, debugPutSession = false;
    private String TAG;
    private Deque<byte[]> фифошка;

    public StorageData(String TAG) {
        this.TAG = TAG;
        фифошка = new ArrayDeque<>();
    }

    public boolean isEmpty(){
        if (!debugGetSession && debug) {
         boolean isEmpty = фифошка.isEmpty();
         if (!isEmpty) {
             Log.w(TAG, "isEmpty not empty");
         }
         return isEmpty;
        } else {
            return фифошка.isEmpty();
        }
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
        if (!debugGetSession && debug) {
            Log.w(TAG, "getData");
            debugGetSession = true;
        }
        if (debug) {
            byte[] data = фифошка.poll();
            if (data == null) {
                Log.e(TAG, "getData is null");
            }
            return data;
        } else {
            return фифошка.poll();
        }
    }

    public void putData(byte[] bytes) {
        if (!debugPutSession && debug) {
            Log.w(TAG, "putData");
            debugPutSession = true;
        }
        фифошка.offer(bytes);
//      фифошка.push(dataByte); //TODO: уточнить: не тот метод?
    }

    public void clear() {
        if (debug) {
            Log.w(TAG, "clear");
            debugGetSession = false;
            debugPutSession = false;
        }
        фифошка.clear();
    }

}
