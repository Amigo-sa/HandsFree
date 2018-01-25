package by.citech.handsfree.threading;

import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ThreadingManager {

    private static final String STAG = Tags.ThreadManager;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    //--------------------- preparation

    private CraftedThreadPool threadPool;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        threadPool = new CraftedThreadPool(Settings.Common.threadNumber);
    }

    //--------------------- singleton

    private static volatile ThreadingManager instance = null;

    private ThreadingManager() {
    }

    public static ThreadingManager getInstance() {
        if (instance == null) {
            synchronized (ThreadingManager.class) {
                if (instance == null) {
                    instance = new ThreadingManager();
                }
            }
        }
        return instance;
    }

    public void activate() {
        threadPool.activate();
    }

    public void deactivate() {
        threadPool.deactivate();
    }

    //--------------------- main

    boolean addRunnable(Runnable runnable) {
        if (debug) Timber.tag(TAG).i("addRunnable");
        if (runnable == null) {
            if (debug) Timber.tag(TAG).e("addRunnable runnable is null");
            return false;
        }
        threadPool.addRunnable(runnable);
        return true;
    }

}
