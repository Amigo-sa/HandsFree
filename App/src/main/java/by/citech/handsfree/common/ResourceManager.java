package by.citech.handsfree.common;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

public class ResourceManager
        implements IBaseCtrl, IPrepareObject, IBase {

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

    private Collection<IBase> iBases;

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
//      iBases = Collections.synchronizedList(new ArrayList<>());
//      iBases = new CopyOnWriteArrayList<>();
        iBases = new ConcurrentLinkedQueue<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return iBases != null;
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

    //--------------------- IBase

    @Override
    public boolean baseStop() {
        if (debug) Log.w(TAG, "baseStop iBases size before stop is " + iBases.size());
        if (iBases != null) {
            for (IBase iBase : iBases) {
                if (iBase != null) {
                    iBase.baseStop();
                }
                iBases.remove(iBase);
            }
            if (debug) Log.w(TAG, "baseStop iBases size after stop is " + iBases.size());
            iBases.clear();
            if (debug) Log.w(TAG, "baseStop iBases size after clear is " + iBases.size());
        } else {
            Log.e(TAG, "baseStop iBases is null" );
        }
        return false;
    }

    //--------------------- IBaseCtrl

    @Override
    public boolean addBase(IBase iBase) {
        if (debug) Log.i(TAG, "addBase");
        prepareObject();
        if (iBase == null) {
            Log.e(TAG, "addBase iBase is null");
            return false;
        } else if (iBases == null) {
            Log.e(TAG, "addBase iBases is null, prepareObject");
            prepareObject();
        }
        if (iBases == null) {
            Log.e(TAG, "addBase iBases is still null, return");
            return false;
        } else {
            iBases.add(iBase);
        }
        return true;
    }

    @Override
    public boolean removeBase(IBase iBase) {
        if (debug) Log.i(TAG, "removeBase");
        if (iBases == null) {
            Log.w(TAG, "removeBase iBases is null, return");
        }
        if (!iBases.remove(iBase)) {
            Log.w(TAG, "removeBase no such element, return");
        }
        return true;
    }

}
