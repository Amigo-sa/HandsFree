package by.citech.dialog;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import by.citech.R;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;

public class DialogProcessor {

    private static final boolean debug = Settings.debug;
    private static final String TAG = Tags.DIALOG_PROC;

    private AppCompatActivity activity;
    private DialogState currentState;
    private DialogType currentType;
    private AlertDialog currentDialog;
    private Queue<DelayedDialog> delayedDialogs;

    public DialogProcessor(AppCompatActivity activity) {
        this.activity = activity;
        currentState = DialogState.Idle;
        delayedDialogs = new ArrayDeque<>();
    }

    //--------------------- main

    public synchronized void runDialog(DialogType toRun, Map<DialogState, Runnable> toDoMap, String... messages) {
        if (toRun == null || toDoMap == null) {
            Log.e(TAG, "runDialog" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (currentType != null || currentDialog != null) {
            Log.e(TAG, "runDialog another dialog still running");
            addDelayedDialog(new DelayedDialog(toRun, toDoMap, messages));
            return;
        } else {
            currentType = toRun;
        }
        switch (currentType) {
            case Delete:
                dialogDelete(toDoMap);
                break;
            case Save:
                dialogSave(toDoMap);
                break;
            case Connect:
                dialogConnect(toDoMap, messages[0]);
                break;
            case Connecting:
                dialogConnecting(toDoMap);
                break;
            case Disconnect:
                dialogDisconnect(toDoMap);
                break;
            case Disconnecting:
                dialogDisconnecting(toDoMap);
                break;
            case Reconnect:
                dialogReconnect(toDoMap);
                break;
            default:
                break;
        }
    }

    public synchronized void denyDialog(DialogType toDeny) {
        if (currentType == null || currentDialog == null) {
            Log.e(TAG, "denyDialog there is no running currentDialog");
            return;
        }
        if (toDeny == null) {
            Log.e(TAG, "denyDialog" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (currentType == toDeny) {
            if (debug) Log.i(TAG, "denyDialog found currentDialog to deny");
            currentDialog.dismiss();
        }
    }

    private void addDelayedDialog(DelayedDialog delayedDialog) {
        if (debug) Log.i(TAG, "addDelayedDialog");
        if (!delayedDialogs.offer(delayedDialog)) {
            Log.e(TAG, "addDelayedDialog fail to add");
        }
    }

    private void runDelayedDialog() {
        if (debug) Log.i(TAG, "runDelayedDialog");
        if (!delayedDialogs.isEmpty()) {
            if (debug) Log.i(TAG, "runDelayedDialog found delayed currentDialog");
            DelayedDialog delayedDialog = delayedDialogs.poll();
            runDialog(delayedDialog.toRun, delayedDialog.toDoMap, delayedDialog.messages);
        } else {
            if (debug) Log.i(TAG, "runDelayedDialog no delayed dialogs");
        }
    }

    private void onDialogEnd() {
        currentState = DialogState.Idle;
        currentType = null;
        currentDialog = null;
        runDelayedDialog();
    }

    //--------------------- delayedDialogs

    private void dialogReconnect(Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogReconnect");
    }

    private void dialogDisconnecting(Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogDisconnecting");
    }

    private void dialogDisconnect(Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogDisconnect");
    }

    private void dialogConnecting(Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogConnecting");
    }

    private void dialogConnect(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogConnect");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogConnect onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "dialogConnect cancel");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "dialogConnect just dismiss");
                            //toDoMap.get(DialogState.Cancel).run(); //TODO: что делать при клике мимо диалога
                            break;
                        default:
                            Log.e(TAG, "dialogConnect currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.connect_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> dialog.cancel());

        currentDialog = builder.create();
        currentDialog.show();
    }

    private void dialogSave(final Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogSave");
    }

    private void dialogDelete(final Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogDelete");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "tryToDeleteContact onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "tryToDeleteContact cancel");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        case Proceed:
                            if (debug) Log.i(TAG, "tryToDeleteContact delete");
                            toDoMap.get(DialogState.Proceed).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "tryToDeleteContact just dismiss");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        default:
                            Log.e(TAG, "dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        currentDialog = builder.create();
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_proceed, null);

        dialogView.findViewById(R.id.btnProceed).setOnClickListener((v) -> {
            currentState = DialogState.Proceed;
            currentDialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
            currentState = DialogState.Cancel;
            currentDialog.dismiss();
        });

        currentDialog.setView(dialogView);
        currentDialog.show();
    }

    //--------------------- support classes

    private class DelayedDialog {

        private DialogType toRun;
        private Map<DialogState, Runnable> toDoMap;
        private String[] messages;

        private DelayedDialog(DialogType toRun, Map<DialogState, Runnable> toDoMap, String... messages) {
            this.toRun = toRun;
            this.toDoMap = toDoMap;
            this.messages = messages;
        }

    }

}
