package by.citech.handsfree.threading;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CraftedThreadPool
        implements IBase, IPrepareObject, IRunnableCtrl {

    private static final boolean debug = Settings.debug;
    private static final String STAG = Tags.THREADPOOL;
    private static int objCount;
    private final String TAG;
    private static final int minThreadsNumber = 2;
    private static final long QUIZ_PERIOD = 10;

    private Runnable waiting = () -> {
        try {
            Thread.sleep(QUIZ_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private Queue<Runnable> runnables;
    private ThreadShard[] threads;
    private int threadNumber;
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
        this.threadNumber = threadNumber;
        upThreads(this.threadNumber);
    }

    private void upThreads(int threadNumber) {
        if (debug) Log.i(TAG,"upThreads");
        if (threadNumber < minThreadsNumber) {
            threadNumber = minThreadsNumber;
        }
        threads = new ThreadShard[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            threads[i] = new ThreadShard();
        }
    }

    //-------------------------- IBase

    @Override
    public boolean baseStart() {
        prepareObject();
        if (debug) Log.i(TAG,"baseStart");
        if (isActive) {
            Log.e(TAG,"baseStart already active");
            return false;
        }
        isActive = true;
        if (threads == null || threads.length < minThreadsNumber) {
            Log.e(TAG,"baseStart threads is null or thread number less then minThreadsNumber, upThreads");
            upThreads(this.threadNumber);
        }
        if (threads == null || threads.length < 1) {
            Log.e(TAG,"baseStart threads is still null or thread number is less then 1, return");
            return false;
        }
        for (ThreadShard thread : threads) {
            thread.start();
        }
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG,"baseStop");
        if (runnables != null) {
            runnables.clear();
        }
        isActive = false;
        return true;
    }

    //-------------------------- main

    @Override
    public boolean addRunnable(Runnable runnable) {
        if (debug) Log.i(TAG,"addRunnable");
        if (runnables == null) {
            Log.e(TAG,"addRunnable runnables is null, prepareObject");
            prepareObject();
        }
        if (runnables == null) {
            Log.e(TAG,"addRunnable runnables is still null, return");
            return false;
        } else {
            if (!runnables.offer(runnable)) {
                Log.e(TAG, "addRunnable add fail");
                return false;
            }
        }
        return true;
    }

    private synchronized Runnable getAvailableRun() {
        if (runnables == null) {
            Log.e(TAG,"getAvailableRun runnables is null, prepareObject and return waiting");
            prepareObject();
            return waiting;
        } else if (!runnables.isEmpty()) {
            if (Settings.debug) Log.i(TAG,"getAvailableRun run available");
            return runnables.poll();
        } else {
            return waiting;
        }
    }

    private class ThreadShard
            extends Thread {
        @Override
        public void run() {
            if (debug) Log.i(TAG,"ThreadShard run");
            while (isActive) getAvailableRun().run();
        }
    }

}

