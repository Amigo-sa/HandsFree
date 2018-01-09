package by.citech.handsfree.management;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;

public class ResourceManager
        implements IPrepareObject {

    private static final String STAG = Tags.RESOURCE_MANAGER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    //--------------------- preparation

    private Collection<IBase> iBaseStarts, iBaseStartsDelayed;
    private Collection<IBase> iBaseCreates, iBaseCreatesDelayed;
    private volatile boolean isOnDestroy;
    private volatile boolean isOnStop;

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
//      iBaseStarts = Collections.synchronizedList(new ArrayList<>());
//      iBaseStarts = new CopyOnWriteArrayList<>();
        iBaseStarts = new ConcurrentLinkedQueue<>();
        iBaseStartsDelayed = new ConcurrentLinkedQueue<>();
        iBaseCreates = new ConcurrentLinkedQueue<>();
        iBaseCreatesDelayed = new ConcurrentLinkedQueue<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return iBaseStarts != null && iBaseCreates != null;
    }

    //--------------------- singleton

    private static volatile ResourceManager instance = null;

    private ResourceManager() {
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            synchronized (ResourceManager.class) {
                if (instance == null) {
                    instance = new ResourceManager();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- states

    private synchronized boolean procOnDestroy(Boolean isOnDestroy) {
        if (isOnDestroy != null) {
            if (debug) Log.w(TAG, "procOnDestroy is onDestroy: " + isOnDestroy);
            this.isOnDestroy = isOnDestroy;
        }
        return this.isOnDestroy;
    }

    private synchronized boolean procOnStop(Boolean isOnStop) {
        if (isOnStop != null) {
            if (debug) Log.w(TAG, "procOnStop is onStop: " + isOnStop);
            this.isOnStop = isOnStop;
        }
        return this.isOnStop;
    }

    //--------------------- IBase

    public boolean stop() {
        if (debug) Log.w(TAG, "stop iBaseStarts size before stop is " + iBaseStarts.size());
        procOnStop(true);
        for (IBase iBase : iBaseStarts) {
            if (iBase != null) {
                iBase.baseStop();
            } else {
                iBaseStarts.remove(iBase);
            }
        }
        int sizeAfterStop = iBaseStarts.size();
        if (sizeAfterStop != 0) {
            if (debug) Log.w(TAG, "stop iBaseStarts size after stop is " + sizeAfterStop);
        }
        procOnStop(false);
        iBaseStarts.addAll(iBaseStartsDelayed);
        iBaseStartsDelayed.clear();
        return false;
    }

    public boolean destroy() {
        if (debug) Log.w(TAG, "destroy iBaseCreates size before destroy is " + iBaseCreates.size());
        procOnDestroy(true);
        for (IBase iBase : iBaseCreates) {
            if (iBase != null) {
                iBase.baseDestroy();
            } else {
                iBaseCreates.remove(iBase);
            }
        }
        int sizeAfterDestroy = iBaseCreates.size();
        if (sizeAfterDestroy != 0) {
            if (debug) Log.w(TAG, "destroy iBaseCreates size after destroy is " + sizeAfterDestroy);
        }
        procOnDestroy(false);
        iBaseCreates.addAll(iBaseCreatesDelayed);
        iBaseCreatesDelayed.clear();
        return false;
    }

    //--------------------- IBaseCtrl start-stop

    synchronized boolean doBaseStart(IBase iBase) {
        if (iBase == null) {
            Log.e(TAG, "doBaseStart iBase is null, return");
            return false;
        }
        if (procOnStop(null)) {
            if (debug) Log.w(TAG, "doBaseStart register delayed");
            return iBaseStartsDelayed.add(iBase);
        } else {
            if (debug) Log.i(TAG, "doBaseStart register");
            return iBaseStarts.add(iBase);
        }
    }

    synchronized boolean doBaseStop(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseStop unregister");
        if (!iBaseStarts.remove(iBase)) {
            Log.w(TAG, "doBaseStop no such element");
        }
        return true;
    }

    //--------------------- IBaseCtrl create-destroy

    synchronized boolean doBaseCreate(IBase iBase) {
        if (iBase == null) {
            Log.e(TAG, "doBaseCreate iBase is null, return");
            return false;
        }
        if (procOnDestroy(null)) {
            if (debug) Log.w(TAG, "doBaseCreate register delayed");
            return iBaseCreatesDelayed.add(iBase);
        } else {
            if (debug) Log.i(TAG, "doBaseCreate register");
            return iBaseCreates.add(iBase);
        }
    }

    synchronized boolean doBaseDestroy(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseDestroy unregister");
        if (!iBaseCreates.remove(iBase)) {
            Log.w(TAG, "doBaseDestroy no such element");
        }
        return true;
    }

}
