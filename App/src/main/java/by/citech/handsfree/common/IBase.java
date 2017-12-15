package by.citech.handsfree.common;

import android.util.Log;

import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IBase {

    String TAG = Tags.I_BASE;
    String ERR_MSG = StatusMessages.ERR_NOT_OVERRIDED;
    String WRN_MSG = StatusMessages.WRN_NOT_OVERRIDED;

    default void baseStart(IBaseAdder iBaseAdder) {
        Log.w(TAG, "baseStart" + WRN_MSG);
        if (iBaseAdder == null) {
            Log.e(TAG, "baseStart iBaseAdder is null");
            return;
        } else {
            iBaseAdder.addBase(this);
        }
    }

    default void basePause() {
        Log.e(TAG, "basePause" + ERR_MSG);
    }

    default void baseResume() {
        Log.e(TAG, "baseResume" + ERR_MSG);
    }

    default void baseStop() {
        Log.e(TAG, "baseStop" + ERR_MSG);
    }

}
