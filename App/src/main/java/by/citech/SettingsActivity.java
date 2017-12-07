package by.citech;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import by.citech.codec.audio.AudioCodecType;
import by.citech.param.Colors;
import by.citech.param.OpMode;
import by.citech.param.Settings;
import by.citech.param.SettingsDefault;
import by.citech.param.Tags;

public class SettingsActivity
        extends AppCompatActivity {

    private static final String TAG = Tags.ACT_SETTINGS;
    private static final boolean debug = Settings.debug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.prefs_content, new SettingsFragment())
                .commit();

        setupActionBar();
    }

    private void setupActionBar() {
        if (debug) Log.i(TAG, "setupActionBar");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            String title = String.format(Locale.US, "%s",
                    "Settings"
            );
            SpannableString s = new SpannableString(title);
            if (title != null) {
                s.setSpan(new ForegroundColorSpan(Colors.WHITE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new AbsoluteSizeSpan(56), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            actionBar.setTitle(s);
        }
    }

    public static class SettingsFragment
            extends PreferenceFragmentCompatDividers
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
            if (debug) Log.i(TAG, "onCreatePreferencesFix");
            setPreferencesFromResource(R.xml.settings, rootKey);
            prepareOpModePref();
            prepareAudioCodecType();
            prepareBtLatencyMsPref();
            prepareBt2NetFactorPref();
        }

        private void prepareOpModePref() {
            if (debug) Log.i(TAG, "prepareOpModePref");
            ListPreference pref = (ListPreference) findPreference(getString(R.string.opMode));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Common.opMode.getSettingName());
            CharSequence[] entries = {
                    OpMode.Normal.getSettingName(),
                    OpMode.Bt2Bt.getSettingName(),
                    OpMode.AudIn2Bt.getSettingName(),
                    OpMode.Bt2AudOut.getSettingName(),
                    OpMode.AudIn2AudOut.getSettingName(),
                    OpMode.Record.getSettingName()
            };
            CharSequence[] entryValues = {
                    OpMode.Normal.getSettingNumber(),
                    OpMode.Bt2Bt.getSettingNumber(),
                    OpMode.AudIn2Bt.getSettingNumber(),
                    OpMode.Bt2AudOut.getSettingNumber(),
                    OpMode.AudIn2AudOut.getSettingNumber(),
                    OpMode.Record.getSettingNumber()
            };
            pref.setEntries(entries);
            pref.setEntryValues(entryValues);
            CharSequence entry = pref.getEntry();
            if (entry == null || entry.length() == 0) {
                if (debug) Log.i(TAG, "prepareOpModePref entry is null, set to default");
                pref.setValue(SettingsDefault.Common.opMode.getSettingName());
            }
            pref.setSummary(pref.getEntry());
        }

        private void prepareAudioCodecType() {
            if (debug) Log.i(TAG, "prepareAudioCodecType");
            ListPreference pref = (ListPreference) findPreference(getString(R.string.audioCodecType));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.AudioCommon.audioCodecType.getSettingName());
            CharSequence[] entries = {
                    AudioCodecType.Sit_2_1_java.getSettingName(),
                    AudioCodecType.Sit_2_1_native.getSettingName(),
                    AudioCodecType.Sit_3_0_java.getSettingName(),
                    AudioCodecType.Sit_3_0_native.getSettingName()
            };
            CharSequence[] entryValues = {
                    AudioCodecType.Sit_2_1_java.getSettingNumber(),
                    AudioCodecType.Sit_2_1_native.getSettingNumber(),
                    AudioCodecType.Sit_3_0_java.getSettingNumber(),
                    AudioCodecType.Sit_3_0_native.getSettingNumber()
            };
            pref.setEntries(entries);
            pref.setEntryValues(entryValues);
            pref.setSummary(pref.getEntry());
            CharSequence entry = pref.getEntry();
            if (entry == null || entry.length() == 0) {
                if (debug) Log.i(TAG, "prepareAudioCodecType entry is null, set to default");
                pref.setValue(SettingsDefault.AudioCommon.audioCodecType.getSettingName());
            }
            pref.setSummary(pref.getEntry());
        }

        private void prepareBt2NetFactorPref() {
            if (debug) Log.i(TAG, "prepareBt2NetFactorPref");
            EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.bt2NetFactor));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Bluetooth.bt2NetFactor);
            String entry = pref.getText();
            if (entry == null || entry.length() == 0) {
                pref.setText(String.valueOf(SettingsDefault.Bluetooth.bt2NetFactor));
            }
            pref.setSummary(pref.getText());
        }

        private void prepareBtLatencyMsPref() {
            if (debug) Log.i(TAG, "prepareBtLatencyMsPref");
            EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.btLatencyMs));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Bluetooth.btLatencyMs);
            String entry = pref.getText();
            if (entry == null || entry.length() == 0) {
                pref.setText(String.valueOf(SettingsDefault.Bluetooth.btLatencyMs));
            }
            pref.setSummary(pref.getText());
        }

        private void refreshListPref(String s) {
            if (debug) Log.i(TAG, "refreshListPref");
            ListPreference pref = (ListPreference) findPreference(s);
            if (pref == null) {
                Log.e(TAG, "refreshListPref pref is null");
                return;
            }
            pref.setSummary(pref.getEntry());
        }

        private void refreshEditTextPref(String s) {
            EditTextPreference pref = (EditTextPreference) findPreference(s);
            if (pref == null) {
                Log.e(TAG, "refreshListPref pref is null");
                return;
            }
            pref.setSummary(pref.getText());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
            try {
                return super.onCreateView(inflater, container, savedInstanceState);
            } finally {
                setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (debug) Log.i(TAG, "onSharedPreferenceChanged s is " + s);
            if (s == null || s.isEmpty()) {
                Log.e(TAG, "onSharedPreferenceChanged s is illegal");
                return;
            }
            switch (s) {
                case SettingsDefault.Key.opMode:
                case SettingsDefault.Key.audioCodecType:
                    refreshListPref(s);
                    break;
                case SettingsDefault.Key.bt2NetFactor:
                case SettingsDefault.Key.btLatencyMs:
                    refreshEditTextPref(s);
                    break;
            }
        }
    }

}
