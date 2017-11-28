package by.citech.dialog;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Map;

import by.citech.R;

public class DialogProcessor {

    private static final boolean debug = true;
    private static final String TAG = "WSD_DialogConstructor";
    private AppCompatActivity activity;
    private DialogState state;

    public DialogProcessor(AppCompatActivity activity) {
        this.activity = activity;
        state = DialogState.Idle;
    }

    public void runDialog(DialogType type, Map<DialogState, Runnable> map) {
        if (map == null || type == null) {
            if (debug) Log.i(TAG, "runDialog one of key parameters are null");
            return;
        }
        switch (type) {
            case Delete:
                dialogDelete(map);
                break;
            case Save:
                dialogSave(map);
                break;
            default:
                break;
        }
    }

    private void dialogSave(final Map<DialogState, Runnable> map) {
    }

    private void dialogDelete(final Map<DialogState, Runnable> map) {
        if (debug) Log.i(TAG, "dialogDelete");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setOnDismissListener((dialog) -> {
                    if (debug) Log.i(TAG, "tryToDeleteContact onDismiss");
                    switch (state) {
                        case Cancel:
                            if (debug) Log.i(TAG, "tryToDeleteContact cancel");
                            map.get(DialogState.Cancel).run();
                            break;
                        case Proceed:
                            if (debug) Log.i(TAG, "tryToDeleteContact delete");
                            map.get(DialogState.Proceed).run();
                            break;
                        case Idle:
                            if (debug) Log.i(TAG, "tryToDeleteContact just dismiss");
                            map.get(DialogState.Cancel).run();
                            break;
                        default:
                            Log.e(TAG, "dialogDelete state default");
                            break;

                    }
                    state = DialogState.Idle;
                });

        final AlertDialog dialog = builder.create();
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_proceed, null);

        dialogView.findViewById(R.id.btnProceed).setOnClickListener((v) -> {
            state = DialogState.Proceed;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener((v) -> {
            state = DialogState.Cancel;
            dialog.dismiss();
        });

        dialog.setView(dialogView);
        dialog.show();
    }

}