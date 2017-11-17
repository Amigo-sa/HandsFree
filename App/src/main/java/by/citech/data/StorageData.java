package by.citech.data;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

import by.citech.param.Settings;

public class StorageData<T> {
    private static final boolean debug = Settings.debug;
    private boolean debugGetSession, debugPutSession;
    private String TAG;
    private Queue<T> фифошка;
    private boolean isWriteLocked;

    public StorageData(String TAG) {
        this.TAG = TAG;
        фифошка = new ArrayDeque<>();
        debugGetSession = false;
        debugPutSession = false;
        isWriteLocked = false;
    }

    public void setWriteLocked(boolean writeLocked) {
        isWriteLocked = writeLocked;
    }

    public boolean isEmpty() {
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

    public T getData() {
        if (!debugGetSession && debug) {
            Log.w(TAG, "getData");
            debugGetSession = true;
        }
        if (debug) {
            T dataOut = фифошка.poll();
            if (dataOut == null) {
                Log.e(TAG, "getData is null");
            }
            return dataOut;
        } else {
            return фифошка.poll();
        }
    }

    public void putData(T dataIn) {
        if (isWriteLocked) return;
        if (!debugPutSession && debug) {
            Log.w(TAG, "putData");
            debugPutSession = true;
        }
        фифошка.offer(dataIn);
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
