package by.citech.handsfree;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.SettingsDefault;
import by.citech.handsfree.parameters.Tags;

public class SettingsActivity
        extends AppCompatActivity {

    private static final String TAG = Tags.SettingsActivity;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(debug) Log.i(TAG,"onOptionsItemSelected");
        super.onBackPressed();
        return true;
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
            prepareAudioCodecTypePref();
            prepareBtLatencyMsPref();
            prepareBt2NetFactorPref();
            prepareBt2btPacketSizePref();
        }

        private void prepareOpModePref() {
            if (debug) Log.i(TAG, "prepareOpModePref");
            ListPreference pref = (ListPreference) findPreference(getString(R.string.opMode));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Common.opMode.getSettingName());
            CharSequence[] entries = {
                    EOpMode.Normal.getSettingName(),
                    EOpMode.Bt2Bt.getSettingName(),
                    EOpMode.DataGen2Bt.getSettingName(),
                    EOpMode.AudIn2Bt.getSettingName(),
                    EOpMode.Bt2AudOut.getSettingName(),
                    EOpMode.AudIn2AudOut.getSettingName(),
                    EOpMode.Record.getSettingName()
            };
            CharSequence[] entryValues = {
                    EOpMode.Normal.getSettingNumber(),
                    EOpMode.Bt2Bt.getSettingNumber(),
                    EOpMode.DataGen2Bt.getSettingNumber(),
                    EOpMode.AudIn2Bt.getSettingNumber(),
                    EOpMode.Bt2AudOut.getSettingNumber(),
                    EOpMode.AudIn2AudOut.getSettingNumber(),
                    EOpMode.Record.getSettingNumber()
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

        private void prepareAudioCodecTypePref() {
            if (debug) Log.i(TAG, "prepareAudioCodecTypePref");
            ListPreference pref = (ListPreference) findPreference(getString(R.string.audioCodecType));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.AudioCommon.audioCodecType.getSettingName());
            CharSequence[] entries = {
                    EAudioCodecType.Sit_2_1_java.getSettingName(),
                    EAudioCodecType.Sit_2_1_native.getSettingName(),
                    EAudioCodecType.Sit_3_0_java.getSettingName(),
                    EAudioCodecType.Sit_3_0_native.getSettingName()
            };
            CharSequence[] entryValues = {
                    EAudioCodecType.Sit_2_1_java.getSettingNumber(),
                    EAudioCodecType.Sit_2_1_native.getSettingNumber(),
                    EAudioCodecType.Sit_3_0_java.getSettingNumber(),
                    EAudioCodecType.Sit_3_0_native.getSettingNumber()
            };
            pref.setEntries(entries);
            pref.setEntryValues(entryValues);
            pref.setSummary(pref.getEntry());
            CharSequence entry = pref.getEntry();
            if (entry == null || entry.length() == 0) {
                if (debug) Log.i(TAG, "prepareAudioCodecTypePref entry is null, set to default");
                pref.setValue(SettingsDefault.AudioCommon.audioCodecType.getSettingName());
            }
            pref.setSummary(pref.getEntry());
        }

        private void prepareBt2NetFactorPref() {
            if (debug) Log.i(TAG, "prepareBt2NetFactorPref");
            EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.bt2NetFactor));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Common.bt2NetFactor);
            String entry = pref.getText();
            if (entry == null || entry.length() == 0) {
                pref.setText(String.valueOf(SettingsDefault.Common.bt2NetFactor));
            }
            pref.setSummary(pref.getText());
        }

        private void prepareBt2btPacketSizePref() {
            if (debug) Log.i(TAG, "prepareBt2btPacketSizePref");
            EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.bt2BtPacketSize));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Bluetooth.bt2BtPacketSize);
            String entry = pref.getText();
            if (entry == null || entry.length() == 0) {
                pref.setText(String.valueOf(SettingsDefault.Bluetooth.bt2BtPacketSize));
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
                case SettingsDefault.TypeName.opMode:
                case SettingsDefault.TypeName.audioCodecType:
                    refreshListPref(s);
                    break;
                case SettingsDefault.TypeName.bt2NetFactor:
                case SettingsDefault.TypeName.btLatencyMs:
                case SettingsDefault.TypeName.bt2BtPacketSize:
                    refreshEditTextPref(s);
                    break;
            }
        }
    }

}
