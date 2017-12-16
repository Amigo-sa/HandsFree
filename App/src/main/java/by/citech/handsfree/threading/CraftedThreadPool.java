package by.citech.handsfree.threading;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IBase;
import by.citech.handsfree.common.IBaseCtrl;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CraftedThreadPool
        implements IBase {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.THREADPOOL;
    private static final long QUIZ_PERIOD = 10;

    private Queue<Runnable> runnables;
    private ThreadShard[] threads;
    private boolean isActive;

    private CraftedThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public CraftedThreadPool(int threadNumber) {
        threads = new ThreadShard[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            threads[i] = new ThreadShard();
        }
        runnables = new ConcurrentLinkedQueue<>();
    }

    //-------------------------- IBase

    @Override
    public void baseStart(IBaseCtrl iBaseCtrl) {
        if (isActive) {
            Log.e(TAG,"baseStart already active");
            return;
        }
        if (iBaseCtrl == null) {
            Log.e(TAG, "baseStart iBaseCtrl is null");
            return;
        } else {
            iBaseCtrl.addBase(this);
        }
        isActive = true;
        for (ThreadShard thread : threads) {
            thread.start();
        }
    }

    @Override
    public void baseStop() {
        isActive = false;
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

