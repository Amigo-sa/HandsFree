package by.citech.threading;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.logic.IBase;

public class CraftedThreadPool
        implements IBase {

    private static final long QUIZ_PERIOD = 10;

    private Queue<Runnable> runnables;
    private ThreadShard[] threads;
    private boolean isActive;

    public CraftedThreadPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public CraftedThreadPool(int threadNumber) {
        threads = new ThreadShard[threadNumber];
        runnables = new ConcurrentLinkedQueue<>();
    }

    //-------------------------- IBase

    @Override
    public void baseStart() {
        if (isActive) return;
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
        runnables.offer(runnable);
    }

    private synchronized Runnable getAvailableRun() {
        if (!runnables.isEmpty()) {
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
            while (isActive) {
                getAvailableRun().run();
            }
        }
    }

}

