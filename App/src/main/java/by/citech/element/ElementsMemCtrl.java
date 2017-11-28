package by.citech.element;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import by.citech.contact.ICopy;
import by.citech.contact.IIdentifier;


public class ElementsMemCtrl <T extends Comparable<T> & IIdentifier & ICopy<T>> {

    private static final boolean debug = true;
    private static final String TAG = "WSD_ElementsMemCtrl";
    private List<T> elements;

    public ElementsMemCtrl() {
        elements = new ArrayList<>(0);
    }

    public List<T> getElements() {
        if (debug) Log.i(TAG, "getElements");
        return elements;
    }

    //--------------------- main

    public void initiate(List<T> entry) {
        if (debug) Log.i(TAG, "initiate");
        elements.clear();
        elements.addAll(entry);
        if (debug) {Log.d(TAG, "elements is (before sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
        Collections.sort(elements);
        if (debug) {Log.d(TAG, "elements is (after sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
    }

    public void add(T entry) {
        if (debug) Log.i(TAG, "add");
        elements.add(entry);
        if (debug) {Log.d(TAG, "elements is (before sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
        Collections.sort(elements);
        if (debug) {Log.d(TAG, "elements is (after sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
    }

    public void delete(T entry) {
        if (debug) Log.i(TAG, "delete");
        if (elements.contains(entry)) {
            if (debug) Log.i(TAG, "delete found element");
            elements.remove(entry);
        } else {
            if (debug) Log.e(TAG, "delete no such element");
        }
        if (debug) {Log.d(TAG, "elements is: "); for (T t : elements) {Log.d(TAG, t.toString());}}
    }

    public void update(T entryToUpd, T entryToCopy) {
        if (debug) Log.i(TAG, "update");
        if (elements.contains(entryToUpd)) {
            if (debug) Log.i(TAG, "update found element");
            entryToUpd.doCopy(entryToCopy);
        } else {
            if (debug) Log.e(TAG, "update no such element");
        }
        if (debug) {Log.d(TAG, "elements is (before sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
        if (entryToCopy.compareTo(entryToUpd) == 0) {
            if (debug) Log.i(TAG, "update need to sort");
            Collections.sort(elements);
        } else {
            if (debug) Log.i(TAG, "update no need to sort");
        }
        if (debug) {Log.d(TAG, "elements is (after sort): "); for (T t : elements) {Log.d(TAG, t.toString());}}
    }

    public boolean checkForUniqueness(T entry) {
        if (debug) Log.i(TAG, "checkForUniqueness");
        for (T t : elements) {
            if (t.equals(entry)) {
                if (debug) Log.w(TAG, "checkForUniqueness not unique");
                return false;
            }
        }
        if (debug) Log.i(TAG, "checkForUniqueness unique");
        return true;
    }

}
