package by.citech.handsfree.common;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import by.citech.handsfree.param.Tags;
import by.citech.handsfree.settings.Settings;

public class ResourceManager
        implements IBaseCtrl, IPrepareObject, IBase {

    private static final String STAG = Tags.RESOURCE_MANAGER;
    private static final boolean debug = Settings.debug;

    private static int objCount;
    private final String TAG;
    static {objCount = 0;}

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    private List<IBase> list;

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        list = new ArrayList<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return (list != null);
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
        }
        return instance;
    }

    //--------------------- IBase

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        if (list != null) {
            for (IBase iBase : list) {
                if (iBase != null) {
                    iBase.baseStop();
                }
            }
            list.clear();
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
        } else if (list == null) {
            Log.e(TAG, "addBase list is null, prepareObject");
            prepareObject();
        }
        if (list == null) {
            Log.e(TAG, "addBase list is still null, return");
            return false;
        } else {
            list.add(iBase);
        }
        return true;
    }

    @Override
    public boolean removeBase(IBase iBase) {
        if (debug) Log.i(TAG, "removeBase");
        if (list == null) {
            Log.w(TAG, "removeBase list is null, return");
        }
        if (!list.remove(iBase)) {
            Log.w(TAG, "removeBase no such element, return");
        }
        return true;
    }

}
