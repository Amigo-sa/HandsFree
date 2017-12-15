package by.citech.handsfree.dialog;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Map;

import by.citech.handsfree.R;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.param.StatusMessages;
import by.citech.handsfree.param.Tags;

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
        if (toRun == null) {
            Log.e(TAG, "runDialog" + StatusMessages.ERR_PARAMETERS);
            return;
        }
        denyDialog(null, null);
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
                dialogDisconnect(toDoMap, messages[0]);
                break;
            case Disconnecting:
                dialogDisconnecting(toDoMap, messages[0]);
                break;
            case Reconnect:
                dialogReconnect(toDoMap, messages[0]);
                break;
            default:
                break;
        }
    }

    public synchronized void denyDialog(DialogType toDeny, DialogState onDeny) {
        if (debug) Log.i(TAG, "denyDialog");
        if (currentDialog == null) {
            if (debug) Log.i(TAG, "denyDialog currentDialog is null, return");
            return;
        } else if (!currentDialog.isShowing()) {
            if (debug) Log.i(TAG, "denyDialog there is no running dialog, return");
            return;
        } else if (toDeny != null) {
            if (currentType == toDeny) {
                if (debug) Log.i(TAG, "denyDialog found dialog to deny, deny");
                if (onDeny != null) {
                    currentState = onDeny;
                }
            } else if (currentType != null) {
                if (debug) Log.i(TAG, "denyDialog not found dialog to deny, return");
                return;
            } else {
                if (debug) Log.i(TAG, "denyDialog currentType is null, deny any dialog");
            }
        } else {
            if (debug) Log.i(TAG, "denyDialog deny any dialog");
        }
        currentDialog.dismiss();
    }

    private void onDialogEnd() {
        if (debug) Log.i(TAG, "onDialogEnd");
        currentState = DialogState.Idle;
        currentType = null;
    }

    //--------------------- delayedDialogs

    private void dialogReconnect(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogReconnect");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "dialogDelete cancel");
                            break;
                        case Proceed:
                            if (debug) Log.i(TAG, "dialogDelete delete");
                            toDoMap.get(DialogState.Proceed).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "dialogDelete just dismiss");
                            break;
                        default:
                            Log.e(TAG, "dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });
        builder.setTitle(deviceName)
               .setMessage(R.string.click_other_device_message)
               .setIcon(android.R.drawable.ic_dialog_info)
               .setPositiveButton(R.string.connect, (dialog, which) -> {
                   currentState = DialogState.Proceed;
                   dialog.dismiss();
               })
               .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                   currentState = DialogState.Cancel;
                   dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }

    private void dialogDisconnecting(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogDisconnecting");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "dialogDelete cancel");
                            break;
                        case Proceed:
                            if (debug) Log.i(TAG, "dialogDelete delete");
                            toDoMap.get(DialogState.Proceed).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "dialogDelete just dismiss");
                            break;
                        default:
                            Log.e(TAG, "dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.click_connected_device_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.disconnect, (dialog, which) -> {
                    currentState = DialogState.Proceed;
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                    currentState = DialogState.Cancel;
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }


    private void dialogDisconnect(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogDisconnect");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogConnect just dismiss");
                    toDoMap.get(DialogState.Idle).run();
                    onDialogEnd();
                });
        builder.setTitle(deviceName)
                .setMessage(R.string.disconnected_message)
                .setIcon(android.R.drawable.ic_lock_power_off)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;

    }

    private void dialogConnect(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogConnect");

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

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }

    private void dialogConnecting(Map<DialogState, Runnable> toDoMap, String deviceName) {
        if (debug) Log.i(TAG, "dialogConnecting");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogConnecting onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "dialogConnecting cancel");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "dialogConnecting just dismiss");
                            break;
                        default:
                            Log.e(TAG, "dialogConnecting currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.connect_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                    currentState = DialogState.Cancel;
                    dialog.cancel();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }

    private void dialogSave(final Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogSave");
    }

    private void dialogDelete(final Map<DialogState, Runnable> toDoMap) {
        if (debug) Log.i(TAG, "dialogDelete");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            if (debug) Log.i(TAG, "dialogDelete cancel");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        case Proceed:
                            if (debug) Log.i(TAG, "dialogDelete delete");
                            toDoMap.get(DialogState.Proceed).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "dialogDelete just dismiss");
                            toDoMap.get(DialogState.Cancel).run();
                            break;
                        default:
                            Log.e(TAG, "dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        AlertDialog dialog = builder.create();
        currentDialog = dialog;

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_proceed, null);

        dialogView.findViewById(R.id.btnProceed).setOnClickListener((v) -> {
            currentState = DialogState.Proceed;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
            currentState = DialogState.Cancel;
            dialog.dismiss();
        });

        dialog.setView(dialogView);
        dialog.show();
    }

}
