package by.citech.handsfree.threading;

import android.util.Log;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.Tags;

public class CraftedThreadPool
        implements IPrepareObject, IRunnableCtrl {

    private static final boolean debug = Settings.debug;
    private static final String STAG = Tags.THREADPOOL;
    private static int objCount;
    private final String TAG;
    private static final int minThreadNumber = 2;
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

    private Queue<ThreadShard> threads;
    private Queue<Runnable> runnables;

    private volatile boolean isActive;
    private volatile int initialThreadNumber;
    private volatile int idleThreadNumber;
    private volatile int runningExtraThreadNumber;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        idleThreadNumber = 0;
        threads = new ConcurrentLinkedQueue<>();
        runnables = new ConcurrentLinkedQueue<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return runnables != null && idleThreadNumber == 0;
    }

    //--------------------- constructor

    private CraftedThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    CraftedThreadPool(int initialThreadNumber) {
        if (initialThreadNumber < minThreadNumber) {
            this.initialThreadNumber = minThreadNumber;
        } else {
            this.initialThreadNumber = initialThreadNumber;
        }
    }

    //-------------------------- activate/deactivate

    public boolean activate() {
        prepareObject();
        if (debug) Log.i(TAG,"activate");
        if (isActive) {
            Log.e(TAG,"activate already active");
            return false;
        }
        isActive = true;
        upThread();
        return true;
    }

    public boolean deactivate() {
        if (debug) Log.i(TAG,"deactivate");
        if (runnables != null) {
            runnables.clear();
        }
        if (threads != null) {
            for (ThreadShard thread : threads) {
                if (thread != null) {
                    thread.deactivate();
                }
            }
        }
        isActive = false;
        return true;
    }

    //-------------------------- main

    private synchronized boolean upThread() {
        if (debug) Log.i(TAG,"upThread");
        if (procThreadNumber(null, null) < initialThreadNumber && isActive) {
            new ThreadShard().start();
            return true;
        } else {
            return false;
        }
    }

    private synchronized int procThreadNumber(Event eventToProcess, ThreadShard threadToProcess) {
        if (eventToProcess != null && threadToProcess != null) {
            switch (eventToProcess) {
                case plusOneRunning:
                    if (!threads.offer(threadToProcess)) {
                        if (debug) Log.i(TAG, "procThreadNumber add thread failed");
                    } else {
                        idleThreadNumber = ++idleThreadNumber;
                    }
                    upThread();
                    break;
                case minusOneRunning:
                    if (!threads.remove(threadToProcess)) {
                        if (debug) Log.i(TAG, "procThreadNumber remove thread failed");
                    } else {
                        idleThreadNumber = --idleThreadNumber;
                    }
                case plusOneIdle:
                    if (threads.contains(threadToProcess)) {
                        idleThreadNumber = ++idleThreadNumber;
                    }
                    break;
                case minusOneIdle:
                    if (threads.contains(threadToProcess)) {
                        idleThreadNumber = --idleThreadNumber;
                    }
                    break;
            }
            int runningThreadNumber = threads.size();
            if (idleThreadNumber < 1 && runningThreadNumber > 0) {
                if (debug) Log.w(TAG, String.format(Locale.US,
                        "procThreadNumber %d of %d threads is idle",
                        idleThreadNumber, runningThreadNumber));
            } else {
                if (debug) Log.i(TAG, String.format(Locale.US,
                        "procThreadNumber %d of %d threads is idle",
                        idleThreadNumber, runningThreadNumber));
            }
            return runningThreadNumber;
        } else {
            return threads.size();
        }
    }

    private synchronized void procExtraThread(Event eventToProcess) {
        if (eventToProcess != null) {
            switch (eventToProcess) {
                case plusOneRunning:
                    runningExtraThreadNumber = ++runningExtraThreadNumber;
                    break;
                case minusOneRunning:
                    runningExtraThreadNumber = --runningExtraThreadNumber;
                    break;
            }
        }
        if (debug) Log.w(TAG, "procExtraThread running extra threads: " + runningExtraThreadNumber);
    }

    @Override
    public boolean addRunnable(Runnable runnable) {
        if (debug) Log.i(TAG, "addRunnable");
        if (runnable == null) {
            Log.e(TAG, "addRunnable runnable is null, return");
            return false;
        } else if (!runnables.offer(runnable)) {
            Log.e(TAG, "addRunnable add fail");
            return false;
        } else if (idleThreadNumber < 1) {
            if (!upThread()) {
                new ExtraThreadShard().start();
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
        private Runnable toRun;
        private boolean isActive;
        @Override
        public void run() {
            if (debug) Log.i(TAG,"ThreadShard run");
            procThreadNumber(Event.plusOneRunning, this);
            isActive = true;
            while (isActive) {
                toRun = getAvailableRun();
                if (toRun == null) {
                    if (debug) Log.w(TAG,"ThreadShard run toRun is null, return");
                    waiting.run();
                } else if (toRun != waiting) {
                    procThreadNumber(Event.minusOneIdle, this);
                    toRun.run();
                    procThreadNumber(Event.plusOneIdle, this);
                } else {
                    toRun.run();
                }
            }
            procThreadNumber(Event.minusOneRunning, this);
        }
        private void deactivate() {isActive = false;}
    }

    private class ExtraThreadShard
            extends Thread {
        Runnable toRun;
        @Override
        public void run() {
            if (debug) Log.w(TAG, "ExtraThreadShard run");
            procExtraThread(Event.plusOneRunning);
            toRun = getAvailableRun();
            if (toRun == null) {
                if (debug) Log.w(TAG, "ExtraThreadShard run toRun is null, return");
            } else if (toRun != waiting) {
                toRun.run();
            }
            procExtraThread(Event.minusOneRunning);
        }
    }

    private enum Event {
        minusOneIdle,
        plusOneIdle,
        minusOneRunning,
        plusOneRunning
    }

}