package by.citech.handsfree.gui.helper;

import android.widget.TextView;

public interface IViewKeeper {

    default void freezeState(String key, TextView... textViews) {
        ViewKeeper.getInstance().freezeState(key, textViews);
    }

    default void releaseState(String key) {
        ViewKeeper.getInstance().releaseState(key);
    }

}