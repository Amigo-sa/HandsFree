package by.citech.param;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import by.citech.R;

public class PreferencesProcessor {

    private static final String TAG = Tags.PREF_PROCESSOR;
    private static final boolean debug = Settings.debug;

    private Context context;
    private SharedPreferences prefs;

    private OpMode opMode;

    public PreferencesProcessor(Context context) {
        this.context = context;
    }

    public void processPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        processOpMode();
    }

    private void processOpMode() {
        String currentOpMode = prefs.getString(context.getString(R.string.opMode), context.getResources().getStringArray(R.array.opMode)[0]);
        for (OpMode mode : OpMode.values()) {
            if (currentOpMode.matches(mode.getSettingName())) {
                if (debug) Log.i(TAG, "found matching opMode: " + mode.getSettingName());
                opMode = mode;
                Presetter.setOpMode(opMode);
                break;
            }
        }
        if (opMode == null) {
            Log.e(TAG, "no matches for opMode, set to default");
            Presetter.setOpModeDefault();
        }
    }
}
