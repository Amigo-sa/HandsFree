package by.citech.handsfree.ui;

public interface ISwipeListener {

    void onSwipe(SwipeDirection direction);

    enum SwipeDirection {
        UP, DOWN, RIGH, LEFT
    }

}
