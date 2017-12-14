package by.citech.handsfree.gui;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;

public interface IGetView {
    @Nullable
    <T extends View> T getView(@IdRes int id);
    @Nullable
    Animation getAnimation(int id);
}
