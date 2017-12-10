package by.citech.logic;

import android.util.Log;

import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public interface IBase {
    String TAG = Tags.I_BASE;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;
    default void baseStart() {Log.e(TAG, "baseStart" + MSG);}
    default void basePause() {Log.e(TAG, "basePause" + MSG);}
    default void baseResume() {Log.e(TAG, "baseResume" + MSG);}
    default void baseStop() {Log.e(TAG, "baseStop" + MSG);}
}
