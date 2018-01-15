package by.citech.handsfree.element;

import android.util.Log;

import java.util.Collections;
import java.util.List;

import by.citech.handsfree.common.ICopy;
import by.citech.handsfree.common.IIdentifier;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.Tags;

public class ElementsMemCtrl <T extends Comparable<T> & IIdentifier & ICopy<T>> {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ElementsMemCtrl;

    private List<T> elements;

    public ElementsMemCtrl(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getList() {
        if (debug) Log.i(TAG, "getList");
        return elements;
    }

    //--------------------- main

    public void sort() {
        if (debug) Log.i(TAG, "sort");
        if (debug) {Log.d(TAG, "sort before: "); for (T t : elements) {Log.d(TAG, t.toString());}}
        Collections.sort(elements);
        if (debug) {Log.d(TAG, "sort after: "); for (T t : elements) {Log.d(TAG, t.toString());}}
    }

    public boolean add(T entry) {
        if (debug) Log.i(TAG, "add");
        if (elements.add(entry)) {
            if (debug) Log.i(TAG, "add success");
            sort();
            return true;
        } else {
            Log.e(TAG, "add fail");
            return false;
        }
    }

    public boolean delete(T entry) {
        if (debug) Log.i(TAG, "delete");
        if (elements.remove(entry)) {
            if (debug) Log.i(TAG, "delete success");
            return true;
        } else {
            Log.e(TAG, "delete fail");
            return false;
        }
    }

    public boolean update(T entryToUpd, T entryToCopy) {
        if (debug) Log.i(TAG, "update");
        if (elements.contains(entryToUpd)) {
            if (debug) Log.i(TAG, "update found element");
            entryToUpd.doCopy(entryToCopy);
        } else {
            Log.e(TAG, "update no such element");
            return false;
        }
        if (entryToCopy.compareTo(entryToUpd) == 0) {
            if (debug) Log.i(TAG, "update need to sort");
            sort();
        } else {
            if (debug) Log.i(TAG, "update no need to sort");
        }
        return true;
    }

    public boolean checkForUniq(T entry) {
        if (debug) Log.i(TAG, "checkForUniq");
        for (T t : elements) {
            if (t.equals(entry)) {
                if (debug) Log.w(TAG, "checkForUniq not unique");
                return false;
            }
        }
        if (debug) Log.i(TAG, "checkForUniq unique");
        return true;
    }

}
