package by.citech.handsfree.data;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;

import by.citech.handsfree.param.Settings;

public class StorageData<T> {

    private static final boolean debug = Settings.debug;
    private boolean debugGetSession, debugPutSession;
    private String TAG;
    private Queue<T> фифошка;
    private boolean isWriteLocked;

    public StorageData(String TAG) {
        this.TAG = TAG;
        фифошка = new ArrayDeque<>();
    }

    public void setWriteLocked(boolean writeLocked) {
        isWriteLocked = writeLocked;
    }

    public boolean isEmpty() {
        if (!debugGetSession && debug) {
            boolean isEmpty = фифошка.isEmpty();
            if (!isEmpty) {
                Log.w(TAG, "isEmpty not empty first after clear");
            }
            return isEmpty;
        } else {
            return фифошка.isEmpty();
        }
    }

    public T getData() {
        if (!debugGetSession && debug) {
            Log.w(TAG, "getData first after clear");
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
            Log.w(TAG, "putData first after clear");
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
