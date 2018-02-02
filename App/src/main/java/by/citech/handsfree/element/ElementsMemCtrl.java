package by.citech.handsfree.element;

import java.util.Collections;
import java.util.List;

import by.citech.handsfree.common.ICopy;
import by.citech.handsfree.common.IIdentifier;
import by.citech.handsfree.parameters.Tags;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ElementsMemCtrl <T extends Comparable<T> & IIdentifier & ICopy<T>> {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.ElementsMemCtrl;

    private List<T> elements;

    public ElementsMemCtrl(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getList() {
        if (debug) Timber.i("getList");
        return elements;
    }

    //--------------------- main

    public void sort() {
        if (debug) Timber.i("sort");
        if (debug) {Timber.d("sort before: "); for (T t : elements) {Timber.d(t.toString());}}
        Collections.sort(elements);
        if (debug) {Timber.d("sort after: "); for (T t : elements) {Timber.d(t.toString());}}
    }

    public boolean add(T entry) {
        if (debug) Timber.i("add");
        if (elements.add(entry)) {
            if (debug) Timber.i("add success");
            sort();
            return true;
        } else {
            if (debug) Timber.e("add fail");
            return false;
        }
    }

    public boolean delete(T entry) {
        if (debug) Timber.i("delete");
        if (elements.remove(entry)) {
            if (debug) Timber.i("delete success");
            return true;
        } else {
            if (debug) Timber.e("delete fail");
            return false;
        }
    }

    public boolean update(T entryToUpd, T entryToCopy) {
        if (debug) Timber.i("update");
        if (elements.contains(entryToUpd)) {
            if (debug) Timber.i("update found element");
            entryToUpd.doCopy(entryToCopy);
        } else {
            if (debug) Timber.e("update no such element");
            return false;
        }
        if (entryToCopy.compareTo(entryToUpd) == 0) {
            if (debug) Timber.i("update need to sort");
            sort();
        } else {
            if (debug) Timber.i("update no need to sort");
        }
        return true;
    }

    public boolean checkForUniq(T entry) {
        if (debug) Timber.i("checkForUniq");
        for (T t : elements) {
            if (t.equals(entry)) {
                if (debug) Timber.w("checkForUniq not unique");
                return false;
            }
        }
        if (debug) Timber.i("checkForUniq unique");
        return true;
    }

}
