package by.citech.handsfree.exchange;

import android.util.Log;

import java.util.Map;

import by.citech.handsfree.dialog.DialogState;
import by.citech.handsfree.dialog.DialogType;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

public interface IMsgToUi {

    String TAG = Tags.I_MSG2UI;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;

    default void sendToUiToast(boolean isFromUiThread, String message) {
        Log.e(TAG, "sendToUiToast" + MSG);
    }

    default void sendToUiDialog(boolean isFromUiThread, DialogType toRun, Map<DialogState, Runnable> toDoMap, String... messages) {
        Log.e(TAG, "sendToUiDialog" + MSG);
    }

    default void recallFromUiDialog(boolean isFromUiThread, DialogType toDeny, DialogState onDeny) {
        Log.e(TAG, "recallFromUiDialog" + MSG);
    }

    default void sendToUiRunnable(boolean isFromUiThread, Runnable toDo) {
        Log.e(TAG, "sendToUiRunnable" + MSG);
    }

}