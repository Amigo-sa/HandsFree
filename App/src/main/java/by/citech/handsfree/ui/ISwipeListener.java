package by.citech.handsfree.ui;

/**
 * Created by tretyak on 26.01.2018.
 */

public interface ISwipeListener {

    void onSwipe(SwipeDirection direction);

    enum SwipeDirection{
        UP, DOWN, RIGH, LEFT
    }


}
