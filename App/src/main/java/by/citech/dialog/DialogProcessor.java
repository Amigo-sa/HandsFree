package by.citech.dialog;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

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

    public DialogProcessor(AppCompatActivity activity) {
        this.activity = activity;
        currentState = DialogState.Idle;
    }

    //--------------------- main

    public synchronized void runDialog(DialogType toRun, Map<DialogState, Runnable> toDoMap, String... messages) {
        if (debug) Log.i(TAG, "runDialog");
        if (toRun == null || toDoMap == null) {
            Log.e(TAG, "runDialog" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (currentDialog != null) {
            Log.e(TAG, "runDialog another dialog still running");
            currentDialog.dismiss();
        }
        currentType = toRun;
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
                dialogConnecting(toDoMap, messages[0]);
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

    public synchronized void denyDialog(DialogType toDeny, DialogState onDeny) {
        if (debug) Log.i(TAG, "denyDialog");
        if (currentDialog == null) {
            Log.e(TAG, "denyDialog there is no running dialog");
            return;
        }
        if (toDeny == null) {
            Log.e(TAG, "denyDialog" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        if (currentType == toDeny) {
            if (debug) Log.i(TAG, "denyDialog found currentDialog to deny");
            if (onDeny != null) {
                currentState = onDeny;
            }
            currentDialog.dismiss();
        }
    }

    private void onDialogEnd() {
        if (debug) Log.i(TAG, "onDialogEnd");
        currentState = DialogState.Idle;
        currentType = null;
        currentDialog = null;
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

    private void dialogConnect(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogConnecting");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogConnect just dismiss");
                    toDoMap.get(DialogState.Idle).run();
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.connected_message)
                .setIcon(android.R.drawable.checkbox_on_background)
                .setCancelable(true);

        currentDialog = builder.create();
        currentDialog.show();

//        final Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            public void run() {
//                if (currentDialog != null)
//                    currentDialog.dismiss(); // when the task active then close the dialog
//                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
//            }
//        }, 2000); // after 2 second (or 2000 miliseconds), the task will be active.

    }

    private void dialogConnecting(Map<DialogState, Runnable> toDoMap, String deviceName) {
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

}
