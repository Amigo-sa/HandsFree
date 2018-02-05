package by.citech.handsfree.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import by.citech.handsfree.R;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.parameters.StatusMessages;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class DialogProcessor {

    private static final String STAG = Tags.DialogProcessor;
    private static final boolean debug = Settings.debug;
    private static int objCount;
    private final String TAG;
    static {objCount = 0;}
    {objCount++;TAG = STAG + " " + objCount;}

    private Context context;
    private EDialogState currentState;
    private EDialogType currentType;
    private AlertDialog currentDialog;

    public DialogProcessor(Context context) {
        this.context = context;
        currentState = EDialogState.Idle;
    }

    //--------------------- main

    public synchronized void runDialog(EDialogType toRun, Map<EDialogState, Runnable> toDoMap, String... messages) {
        Timber.i("runDialog");
        if (toRun == null) {
            Timber.e("runDialog + %s", StatusMessages.ERR_PARAMETERS);
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
            case Chose:
                dialogChose(toDoMap, messages[0]);
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

    public synchronized void denyDialog(EDialogType toDeny, EDialogState onDeny) {
        Timber.i("denyDialog");
        if (currentDialog == null) {
            Timber.i("denyDialog currentDialog is null, return");
            return;
        } else if (!currentDialog.isShowing()) {
            Timber.i("denyDialog there is no running dialog, return");
            return;
        } else if (toDeny != null) {
            if (currentType == toDeny) {
                Timber.i("denyDialog found dialog to deny, deny");
                if (onDeny != null) {
                    currentState = onDeny;
                }
            } else if (currentType != null) {
                Timber.i("denyDialog not found dialog to deny, return");
                return;
            } else {
                Timber.i("denyDialog currentType is null, deny any dialog");
            }
        } else {
            Timber.i("denyDialog deny any dialog");
        }
        currentDialog.dismiss();
    }

    private void onDialogEnd() {
        Timber.i("onDialogEnd");
        currentState = EDialogState.Idle;
        currentType = null;
    }

    //--------------------- delayedDialogs

    private void dialogReconnect(Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogReconnect");
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            Timber.i("dialogDelete cancel");
                            break;
                        case Proceed:
                            Timber.i("dialogDelete delete");
                            toDoMap.get(EDialogState.Proceed).run();
                            break;
                        case Idle:
                            Timber.i("dialogDelete just dismiss");
                            break;
                        default:
                            Timber.e("dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });
        builder.setTitle(deviceName)
               .setMessage(R.string.click_other_device_message)
               .setIcon(android.R.drawable.ic_dialog_info)
               .setPositiveButton(R.string.connect, (dialog, which) -> {
                   currentState = EDialogState.Proceed;
                   dialog.dismiss();
               })
               .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                   currentState = EDialogState.Cancel;
                   dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }

    private void dialogDisconnecting(Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogDisconnecting");
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            Timber.i("dialogDelete cancel");
                            break;
                        case Proceed:
                            Timber.i("dialogDelete delete");
                            toDoMap.get(EDialogState.Proceed).run();
                            break;
                        case Idle:
                            Timber.i("dialogDelete just dismiss");
                            break;
                        default:
                            Timber.e("dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.click_connected_device_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.disconnect, (dialog, which) -> {
                    currentState = EDialogState.Proceed;
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                    currentState = EDialogState.Cancel;
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }


    private void dialogDisconnect(Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogDisconnect");

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogConnect just dismiss");
                    toDoMap.get(EDialogState.Idle).run();
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

    private void dialogConnect(Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogConnect");

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogConnect just dismiss");
                    toDoMap.get(EDialogState.Idle).run();
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

    private void dialogConnecting(Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogConnecting");

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogConnecting onDismiss");
                    switch (currentState) {
                        case Cancel:
                            Timber.i("dialogConnecting cancel");
                            toDoMap.get(EDialogState.Cancel).run();
                            break;
                        case Idle:
                            Timber.i("dialogConnecting just dismiss");
                            break;
                        default:
                            Timber.e("dialogConnecting currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        builder.setTitle(deviceName)
                .setMessage(R.string.connect_message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel, (dialog, identifier) -> {
                    currentState = EDialogState.Cancel;
                    dialog.cancel();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentDialog = dialog;
    }

    private void dialogSave(final Map<EDialogState, Runnable> toDoMap) {
        Timber.i("dialogSave");
    }

    private void dialogDelete(final Map<EDialogState, Runnable> toDoMap) {
        Timber.i("dialogDelete");

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogDelete onDismiss");
                    switch (currentState) {
                        case Cancel:
                            Timber.i("dialogDelete cancel");
                            toDoMap.get(EDialogState.Cancel).run();
                            break;
                        case Proceed:
                            Timber.i("dialogDelete delete");
                            toDoMap.get(EDialogState.Proceed).run();
                            break;
                        case Idle:
                            Timber.i("dialogDelete just dismiss");
                            toDoMap.get(EDialogState.Cancel).run();
                            break;
                        default:
                            Timber.e("dialogDelete currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        AlertDialog dialog = builder.create();

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_proceed, null);

        dialogView.findViewById(R.id.btnProceed).setOnClickListener((v) -> {
            currentState = EDialogState.Proceed;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
            currentState = EDialogState.Cancel;
            dialog.dismiss();
        });

        dialog.setView(dialogView);
        dialog.show();
        currentDialog = dialog;
    }


    private void dialogChose(final Map<EDialogState, Runnable> toDoMap, String deviceName) {
        Timber.i("dialogChose");
        TextView title;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setOnDismissListener((dialog) -> {
                    Timber.i("dialogChose onDismiss");
                    switch (currentState) {
                        case Cancel:
                            Timber.i("dialogChose cancel");
                            //toDoMap.get(EDialogState.Cancel).run();
                            break;
                        case Proceed:
                            Timber.i("dialogChose to connect");
                            toDoMap.get(EDialogState.Proceed).run();
                            break;
                        case Idle:
                            Timber.i("dialogChose just dismiss");
                           // toDoMap.get(EDialogState.Cancel).run();
                            break;
                        default:
                            Timber.e("dialogChose currentState default");
                            break;
                    }
                    onDialogEnd();
                });

        AlertDialog dialog = builder.create();
        currentDialog = dialog;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_chose, null);

        title = dialogView.findViewById(R.id.dialogTitle);
        title.setText(String.format("Connect to device %s", deviceName));

        dialogView.findViewById(R.id.btnProceed).setOnClickListener((v) -> {
            currentState = EDialogState.Proceed;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
            currentState = EDialogState.Cancel;
            dialog.dismiss();
        });

        dialog.setView(dialogView);
        dialog.show();
    }





}
