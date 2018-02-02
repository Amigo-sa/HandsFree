package by.citech.handsfree.ui;

import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.util.Pair;
import timber.log.Timber;

public class ViewKeeper {

    private static final boolean debug = Settings.debug;

    //--------------------- preparation

    private Map<String, Pair<TextView[], boolean[]>> pairMap;

    //--------------------- singleton

    private static volatile ViewKeeper instance = null;

    private ViewKeeper() {
        pairMap = new HashMap<>();
    }

    public static ViewKeeper getInstance() {
        if (instance == null) {
            synchronized (ViewKeeper.class) {
                if (instance == null) {instance = new ViewKeeper();}}}
        return instance;
    }

    //--------------------- main

    private void freezeState(String key, TextView... textViews) {
        if (key == null || textViews == null || textViews.length < 1) {
            if (debug) Timber.e("freezeState %s", StatusMessages.ERR_PARAMETERS);
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

    private void releaseState(String key) {
        if (key == null) {
            if (debug) Timber.e("releaseState %s", StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (pairMap == null) {
            if (debug) Timber.e("releaseState pairMap is null, return");
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

    //--------------------- interfaces

    public interface IViewKeeper {

        default void freezeState(String key, TextView... textViews) {
            getInstance().freezeState(key, textViews);
        }

        default void releaseState(String key) {
            getInstance().releaseState(key);
        }

    }
}
