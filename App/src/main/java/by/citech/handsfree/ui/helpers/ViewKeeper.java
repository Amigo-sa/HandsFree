package by.citech.handsfree.ui.helpers;

import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.management.IBase;
import by.citech.handsfree.common.IPrepareObject;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.util.Pair;

public class ViewKeeper
        implements IBase, IPrepareObject {

    private static final String STAG = Tags.VIEW_KEEPER;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;

    static {
        objCount = 0;
    }

    //--------------------- preparation

    private Map<String, Pair<TextView[], boolean[]>> pairMap;

    {
        objCount++;
        TAG = STAG + " " + objCount;
        prepareObject();
    }

    @Override
    public boolean prepareObject() {
        if (isObjectPrepared()) return true;
        pairMap = new HashMap<>();
        return isObjectPrepared();
    }

    @Override
    public boolean isObjectPrepared() {
        return pairMap != null;
    }

    //--------------------- singleton

    private static volatile ViewKeeper instance = null;

    private ViewKeeper() {
    }

    public static ViewKeeper getInstance() {
        if (instance == null) {
            synchronized (ViewKeeper.class) {
                if (instance == null) {
                    instance = new ViewKeeper();
                }
            }
        } else {
            instance.prepareObject();
        }
        return instance;
    }

    //--------------------- IBase

    @Override
    public boolean baseStart() {
        IBase.super.baseStart();
        if (debug) Log.i(TAG, "baseStart");
        if (!prepareObject()) {
            Log.e(TAG, "baseStart object is not prepared, return");
            return false;
        }
        return true;
    }

    @Override
    public boolean baseStop() {
        if (debug) Log.i(TAG, "baseStop");
        if (pairMap != null) {
            pairMap.clear();
        }
        IBase.super.baseStop();
        return true;
    }

    //--------------------- main

    public void freezeState(String key, TextView... textViews) {
        if (key == null || textViews == null || textViews.length < 1) {
            Log.e(TAG, "freezeState" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (!prepareObject()) {
            Log.w(TAG, "freezeState object not prepared, return");
            return;
        }
        pairMap.remove(key);
        boolean[] isEnabledArr = new boolean[textViews.length];
        for (int i = 0; i < textViews.length; i++) {
            isEnabledArr[i] = textViews[i].isEnabled();
        }
        pairMap.put(key, new Pair<>(textViews, isEnabledArr));
        ViewHelper.disableGray(textViews);
    }

    public void releaseState(String key) {
        if (key == null) {
            Log.e(TAG, "releaseState" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (pairMap == null) {
            Log.e(TAG, "releaseState pairMap is null, return");
            return;
        }
        Pair<TextView[], boolean[]> pair = pairMap.get(key);
        for (int i = 0; i < pair.getX().length; i++) {
            if (pair.getY()[i]) {
                ViewHelper.enableGreen(pair.getX()[i]);
            } else {
                ViewHelper.disableGray(pair.getX()[i]);
            }
        }
        pairMap.remove(key);
    }

}
