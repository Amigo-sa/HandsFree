package by.citech.handsfree.threading;

import android.util.Log;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.ISettingsCtrl;
import by.citech.handsfree.settings.Settings;

public class ThreadManager
        implements ISettingsCtrl, IBase, IPrepareObject {

    private static final String STAG = Tags.ConnectorNet;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private int threadNumber;
    private CraftedThreadPool threadPool;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        takeSettings();
        if (threadPool == null) {
            threadPool = new CraftedThreadPool(threadNumber);
        }
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return threadPool != null;
    }

    @Override
    public boolean takeSettings() {
        threadNumber = Settings.getInstance().getCommon().getThreadNumber();
        return true;
    }

    //--------------------- singleton

    private static volatile ThreadManager instance = null;

    private ThreadManager() {
    }

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- IBase

    @Override
    public boolean baseCreate() {
        IBase.super.baseCreate();
        if (debug) Log.i(TAG, "baseCreate");
        if (!prepareObject()) {
            Log.i(TAG, "baseCreate threadPool is still null, return");
            return false;
        } else {
            threadPool.activate();
        }
        return true;
    }

    @Override
    public boolean baseDestroy() {
        if (debug) Log.i(TAG, "baseDestroy");
        threadPool.deactivate();
        IBase.super.baseDestroy();
        return true;
    }

    //--------------------- main

    boolean addRunnable(Runnable runnable) {
        if (debug) Log.i(TAG, "addRunnable");
        if (runnable == null) {
            Log.e(TAG, "addRunnable runnable is null");
            return false;
        }
        threadPool.addRunnable(runnable);
        return true;
    }

}
