package by.citech.gui;

import android.support.annotation.IdRes;
import android.view.View;

public interface IGetViewById {
    <T extends View> T findViewById(@IdRes int id);
}
