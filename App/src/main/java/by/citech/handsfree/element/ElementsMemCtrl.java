package by.citech.handsfree.element;

import java.util.Collections;
import java.util.List;

import by.citech.handsfree.common.ICopy;
import by.citech.handsfree.common.IIdentifier;
import by.citech.handsfree.settings.Settings;
import timber.log.Timber;

public class ElementsMemCtrl <T extends Comparable<T> & IIdentifier & ICopy<T>> {

    private static final boolean debug = Settings.debug;

    private List<T> elements;

    public ElementsMemCtrl(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getList() {
        Timber.i("getList");
        return elements;
    }

    //--------------------- main

    public void sort() {
        Timber.i("sort");
        Timber.d("sort before: "); for (T t : elements) Timber.d(t.toString());
        Collections.sort(elements);
        Timber.d("sort after: "); for (T t : elements) Timber.d(t.toString());
    }

    public boolean add(T entry) {
        Timber.i("add");
        if (elements.add(entry)) {
            Timber.i("add success");
            sort();
            return true;
        } else {
            Timber.e("add fail");
            return false;
        }
    }

    public boolean delete(T entry) {
        Timber.i("delete");
        if (elements.remove(entry)) {
            Timber.i("delete success");
            return true;
        } else {
            Timber.e("delete fail");
            return false;
        }
    }

    public boolean update(T entryToUpd, T entryToCopy) {
        Timber.i("update");
        if (elements.contains(entryToUpd)) {
            Timber.i("update found element");
            entryToUpd.doCopy(entryToCopy);
        } else {
            Timber.e("update no such element");
            return false;
        }
        if (entryToCopy.compareTo(entryToUpd) == 0) {
            Timber.i("update need to sort");
            sort();
        } else {
            Timber.i("update no need to sort");
        }
        return true;
    }

    public boolean checkForUniq(T entry) {
        Timber.i("checkForUniq");
        for (T t : elements) {
            if (t.equals(entry)) {
                Timber.w("checkForUniq not unique");
                return false;
            }
        }
        Timber.i("checkForUniq unique");
        return true;
    }

}
