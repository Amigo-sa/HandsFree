package by.citech.handsfree.threading;

import android.util.Log;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class CraftedThreadPool
        implements IPrepareObject {

    private static final boolean debug = Settings.debug;
    private static final String STAG = Tags.CraftedThreadPool;
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
        threads = new ConcurrentLinkedQueue<>();
        runnables = new ConcurrentLinkedQueue<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return runnables != null && threads != null;
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

    void activate() {
        prepareObject();
        Timber.i("activate");
        if (isActive) {
            Log.e(TAG,"activate already active, return");
            return;
        }
        isActive = true;
        upThread();
    }

    void deactivate() {
        Timber.i("deactivate");
        runnables.clear();
        for (ThreadShard thread : threads) {
            if (thread != null) {
                thread.deactivate();
            } else {
                threads.remove(thread);
            }
        }
        isActive = false;
    }

    //-------------------------- main

    void addRunnable(Runnable runnable) {
        if (runnable == null) {
            Timber.w("addRunnable runnable is null, return");
        } else if (!runnables.offer(runnable)) {
            Timber.w("addRunnable add fail, return");
        } else if (idleThreadNumber < 1) {
            Timber.i("addRunnable low on threads, creating new one");
            if (!upThread()) {
                new ExtraThreadShard().start();
            }
        }
    }

    private synchronized boolean upThread() {
        Timber.i("upThread");
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
                        Timber.i("procThreadNumber add thread failed");
                    } else {
                        idleThreadNumber = ++idleThreadNumber;
                    }
                    upThread();
                    break;
                case minusOneRunning:
                    if (!threads.remove(threadToProcess)) {
                        Timber.i("procThreadNumber remove thread failed");
                    } else {
                        idleThreadNumber = --idleThreadNumber;
                    }
                    break;
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
                Timber.w(String.format(Locale.US,
                        "procThreadNumber %d of %d threads is idle",
                        idleThreadNumber, runningThreadNumber));
            } else {
                Timber.i(String.format(Locale.US,
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
        Timber.w("procExtraThread running extra threads: " + runningExtraThreadNumber);
    }

    private synchronized Runnable getAvailableRun() {
        if (!runnables.isEmpty()) {
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
            Timber.i("ThreadShard run");
            procThreadNumber(Event.plusOneRunning, this);
            isActive = true;
            while (isActive) {
                toRun = getAvailableRun();
                if (toRun == null) {
                    Timber.w("ThreadShard run toRun is null, waiting");
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
            Timber.w("ExtraThreadShard run");
            procExtraThread(Event.plusOneRunning);
            toRun = getAvailableRun();
            if (toRun == null) {
                Timber.w("ExtraThreadShard run toRun is null, return");
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