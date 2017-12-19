package by.citech.handsfree.common;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.param.Tags;
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

    private Collection<IBase> iBaseStarts;
    private Collection<IBase> iBaseCreates;

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
//      iBaseStarts = Collections.synchronizedList(new ArrayList<>());
//      iBaseStarts = new CopyOnWriteArrayList<>();
        iBaseStarts = new ConcurrentLinkedQueue<>();
        iBaseCreates = new ConcurrentLinkedQueue<>();
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

    public boolean isDone() {
        if (iBaseStarts == null) {
            Log.w(TAG, "isDone iBaseStarts is null, done");
            return true;
        } else if (!iBaseStarts.isEmpty()) {
            Log.w(TAG, "isDone iBaseStarts is not done, size is " + iBaseStarts.size());
            return false;
        } else {
            return iBaseStarts.isEmpty();
        }
    }

    public boolean checkIfDoneOrStop() {
        if (!isDone()) {
            stop();
            return false;
        } else {
            return true;
        }
    }

    //--------------------- IBase

    public boolean stop() {
        if (debug) Log.w(TAG, "stop iBaseStarts size before stop is " + iBaseStarts.size());
        if (iBaseStarts != null) {
            for (IBase iBase : iBaseStarts) {
                if (iBase != null) {
                    iBase.baseStop();
                }
//              iBaseStarts.remove(iBase);
            }
            if (debug) Log.w(TAG, "stop iBaseStarts size after stop is " + iBaseStarts.size());
            iBaseStarts.clear();
            if (debug) Log.w(TAG, "stop iBaseStarts size after clear is " + iBaseStarts.size());
        } else {
            Log.e(TAG, "baseStop iBaseStarts is null" );
        }
        return false;
    }

    public boolean destroy() {
        if (debug) Log.w(TAG, "destroy iBaseCreates size before stop is " + iBaseCreates.size());
        if (iBaseCreates != null) {
            for (IBase iBase : iBaseCreates) {
                if (iBase != null) {
                    iBase.baseDestroy();
                }
//              iBaseCreates.remove(iBase);
            }
            if (debug) Log.w(TAG, "destroy iBaseCreates size after stop is " + iBaseCreates.size());
            iBaseCreates.clear();
            if (debug) Log.w(TAG, "destroy iBaseCreates size after clear is " + iBaseCreates.size());
        } else {
            Log.e(TAG, "destroy iBaseCreates is null" );
        }
        return false;
    }

    //--------------------- IBaseCtrl

    boolean doBaseStart(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseStart");
        prepareObject();
        if (iBase == null) {
            Log.e(TAG, "doBaseStart iBase is null");
            return false;
        } else if (iBaseStarts == null) {
            Log.e(TAG, "doBaseStart iBaseStarts is null, prepareObject");
            prepareObject();
        }
        if (iBaseStarts == null) {
            Log.e(TAG, "doBaseStart iBaseStarts is still null, return");
            return false;
        } else {
            iBaseStarts.add(iBase);
        }
        return true;
    }

    boolean doBaseStop(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseStop");
        if (iBaseStarts == null) {
            Log.w(TAG, "doBaseStop iBaseStarts is null");
        } else if (!iBaseStarts.remove(iBase)) {
            Log.w(TAG, "doBaseStop no such element");
        }
        return true;
    }

    boolean doBaseCreate(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseCreate");
        prepareObject();
        if (iBase == null) {
            Log.e(TAG, "doBaseCreate iBase is null");
            return false;
        } else if (iBaseCreates == null) {
            Log.e(TAG, "doBaseCreate iBaseCreates is null, prepareObject");
            prepareObject();
        }
        if (iBaseCreates == null) {
            Log.e(TAG, "doBaseCreate iBaseCreates is still null, return");
            return false;
        } else {
            iBaseCreates.add(iBase);
        }
        return true;
    }

    boolean doBaseDestroy(IBase iBase) {
        if (debug) Log.i(TAG, "doBaseDestroy");
        if (iBaseCreates == null) {
            Log.w(TAG, "doBaseDestroy iBaseCreates is null");
        } else if (!iBaseCreates.remove(iBase)) {
            Log.w(TAG, "doBaseDestroy no such element");
        }
        return true;
    }
}
