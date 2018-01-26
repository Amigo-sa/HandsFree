package by.citech.handsfree.ui;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import by.citech.handsfree.bluetoothlegatt.ui.IUiToBtListener;

/**
 * Created by tretyak on 09.01.2018.
 */

public class LinearLayoutTouchListener implements View.OnTouchListener {

    static final String logTag = "ActivitySwipeDetector";
    // TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private ISwipeListener iSwipeListener;

    public LinearLayoutTouchListener(ISwipeListener iSwipeListener) {
        this.iSwipeListener = iSwipeListener;
    }
    private void onRightToLeftSwipe() {
        iSwipeListener.onSwipe(ISwipeListener.SwipeDirection.LEFT);
    }
    private void onLeftToRightSwipe() {
        iSwipeListener.onSwipe(ISwipeListener.SwipeDirection.RIGH);
    }

    private void onTopToBottomSwipe() {
        iSwipeListener.onSwipe(ISwipeListener.SwipeDirection.DOWN);
    }

    private void onBottomToTopSwipe() {
        iSwipeListener.onSwipe(ISwipeListener.SwipeDirection.UP);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = motionEvent.getX();
                downY = motionEvent.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = motionEvent.getX();
                upY = motionEvent.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;
                // swipe horizontal?
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX < 0) {
                        this.onLeftToRightSwipe();
                        return true;
                    }
                    if (deltaX > 0) {
                        this.onRightToLeftSwipe();
                        return true;
                    }
                } else {
                    Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long horizontally, need at least " + MIN_DISTANCE);
                    // return false; // We don't consume the event
                }
                // swipe vertical?
                if (Math.abs(deltaY) > MIN_DISTANCE) {
                    // top or down
                    if (deltaY < 0) {
                        this.onTopToBottomSwipe();
                        return true;
                    }
                    if (deltaY > 0) {
                        this.onBottomToTopSwipe();
                        return true;
                    }
                } else {
                    Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long vertically, need at least " + MIN_DISTANCE);
                }
                return false; // no swipe horizontally and no swipe vertically
            } // case MotionEvent.ACTION_UP:
        }
        return false;
    }

}
