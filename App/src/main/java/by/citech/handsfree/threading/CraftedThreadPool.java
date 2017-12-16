package by.citech.handsfree.threading;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IBaseCtrl;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CraftedThreadPool
        implements IBase, IPrepareObject {

    private static final boolean debug = Settings.debug;
    private static final String STAG = Tags.THREADPOOL;
    private static int objCount;
    private final String TAG;
    private static final long QUIZ_PERIOD = 10;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private Queue<Runnable> runnables;
    private ThreadShard[] threads;
    private boolean isActive;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        runnables = new ConcurrentLinkedQueue<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return runnables != null;
    }

    //--------------------- constructor

    private CraftedThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public CraftedThreadPool(int threadNumber) {
        threads = new ThreadShard[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            threads[i] = new ThreadShard();
        }
    }

    //-------------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG,"baseStart");
        if (isActive) {
            Log.e(TAG,"baseStart already active");
            return false;
        }
        isActive = true;
        for (ThreadShard thread : threads) {
            thread.start();
        }
        return true;
    }

    @Override
    public boolean baseStop() {
//        IBase.super.baseStop();
        if (debug) Log.i(TAG,"baseStop");
        if (runnables != null) {
            runnables.clear();
            runnables = null;
        }
        threads = null;
        isActive = false;
        return true;
    }

    //-------------------------- main

    public void addRunnable(Runnable runnable) {
        if (debug) Log.i(TAG,"addRunnable");
        if (!runnables.offer(runnable)) {
            Log.e(TAG,"addRunnable add fail");
        }
    }

    private synchronized Runnable getAvailableRun() {
        if (!runnables.isEmpty()) {
            if (Settings.debug) Log.i(TAG,"getAvailableRun run available");
            return runnables.poll();
        } else {
            return () -> {
                try {
                    Thread.sleep(QUIZ_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
        }
    }

    private class ThreadShard
            extends Thread {
        @Override
        public void run() {
            if (debug) Log.i(TAG,"ThreadShard run");
            while (isActive) {
                getAvailableRun().run();
            }
        }
    }

}

