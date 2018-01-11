package by.citech.handsfree.data;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.settings.Settings;

public class StorageData<T> {

    private static final boolean debug = Settings.debug;
    private String TAG;
    private Queue<T> фифошка;
    private boolean isWriteLocked;

    public StorageData(String TAG) {
        this.TAG = TAG;
        фифошка = new ConcurrentLinkedQueue<>();
    }

    public void setWriteLocked(boolean isLocked) {
        isWriteLocked = isLocked;
    }

    public boolean isEmpty() {
        return фифошка.isEmpty();
    }

    public void clear() {
        фифошка.clear();
    }

    public T getData() {
        T dataOut = фифошка.poll();
        if (dataOut == null) {
            if (debug) Log.e(TAG, "getData is null");
        }
        return dataOut;
    }

    public void putData(T dataIn) {
        if (isWriteLocked) return;
        if (dataIn == null) {
            if (debug) Log.e(TAG, "putData is null");
            return;
        }
        фифошка.offer(dataIn);
    }

}
