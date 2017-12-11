package by.citech.gui;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

public interface IGetViewById {
    @Nullable
    <T extends View> T findViewById(@IdRes int id);
}
