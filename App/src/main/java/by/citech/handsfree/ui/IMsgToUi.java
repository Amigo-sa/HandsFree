package by.citech.handsfree.ui;

import android.util.Log;

import java.util.Map;

import by.citech.handsfree.dialog.EDialogState;
import by.citech.handsfree.dialog.EDialogType;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;

public interface IMsgToUi {

    String TAG = Tags.I_MSG2UI;
    String MSG = StatusMessages.ERR_NOT_OVERRIDED;

    default void sendToUiToast(boolean isFromUiThread, String message) {
        Log.e(TAG, "sendToUiToast" + MSG);
    }

    default void sendToUiDialog(boolean isFromUiThread, EDialogType toRun, Map<EDialogState, Runnable> toDoMap, String... messages) {
        Log.e(TAG, "sendToUiDialog" + MSG);
    }

    default void recallFromUiDialog(boolean isFromUiThread, EDialogType toDeny, EDialogState onDeny) {
        Log.e(TAG, "recallFromUiDialog" + MSG);
    }

    default void sendToUiRunnable(boolean isFromUiThread, Runnable toDo) {
        Log.e(TAG, "sendToUiRunnable" + MSG);
    }

}
