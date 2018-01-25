package by.citech.handsfree.activity;

import android.content.Intent;
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

import by.citech.handsfree.R;
import by.citech.handsfree.codec.audio.EAudioCodecType;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.EOpMode;
import by.citech.handsfree.settings.PreferencesProcessor;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.SettingsDefault;
import by.citech.handsfree.parameters.Tags;
import timber.log.Timber;

public class SettingsActivity
        extends AppCompatActivity {

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

    @Override
    public void onBackPressed() {
        intoTheCall();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(debug) Timber.i("onOptionsItemSelected");
        intoTheCall();
        return true;
    }

    private void intoTheCall() {
        startActivity(new Intent(this, CallActivity.class));
        finish();
    }

    //-------------------------- setup

    private void setupActionBar() {
        if (debug) Timber.i("setupActionBar");
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
                s.setSpan(new AbsoluteSizeSpan(40), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            actionBar.setTitle(s);
        }
    }

    //-------------------------- SettingsFragment

    public static class SettingsFragment
            extends PreferenceFragmentCompatDividers
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
            if (debug) Timber.i("onCreatePreferencesFix");
            setPreferencesFromResource(R.xml.settings, rootKey);
            prepareOpModePref();
            prepareAudioCodecTypePref();
            prepareBtLatencyMsPref();
            prepareBt2NetFactorPref();
            prepareBt2btPacketSizePref();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
            try {
                return super.onCreateView(inflater, container, savedInstanceState);
            } finally {
                setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
            }
        }

        //-------------------------- preferences preparation

        private void prepareOpModePref() {
            if (debug) Timber.i("prepareOpModePref");
            ListPreference pref = (ListPreference) findPreference(getString(R.string.opMode));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Common.opMode.getSettingName());
            CharSequence[] entries = {

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
                if (debug) Timber.i("prepareOpModePref entry is null, set to default");
                pref.setValue(SettingsDefault.Common.opMode.getSettingName());
            }
            pref.setSummary(pref.getEntry());
        }

        private void prepareAudioCodecTypePref() {
            if (debug) Timber.i("prepareAudioCodecTypePref");
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
                if (debug) Timber.i("prepareAudioCodecTypePref entry is null, set to default");
                pref.setValue(SettingsDefault.AudioCommon.audioCodecType.getSettingName());
            }
            pref.setSummary(pref.getEntry());
        }

        private void prepareBt2NetFactorPref() {
            if (debug) Timber.i("prepareBt2NetFactorPref");
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
            if (debug) Timber.i("prepareBt2btPacketSizePref");
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
            if (debug) Timber.i("prepareBtLatencyMsPref");
            EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.btLatencyMs));
            if (pref == null) return;
            pref.setDefaultValue(SettingsDefault.Bluetooth.btLatencyMs);
            String entry = pref.getText();
            if (entry == null || entry.length() == 0) {
                pref.setText(String.valueOf(SettingsDefault.Bluetooth.btLatencyMs));
            }
            pref.setSummary(pref.getText());
        }

        //-------------------------- on change refresh

        private String getRefreshedListPref(String prefName) {
            if (debug) Timber.i("refreshListPref");
            ListPreference pref = (ListPreference) findPreference(prefName);
            String newSummary = pref.getValue();
            pref.setSummary(newSummary);
            return newSummary;
        }

        private String getRefreshedEditTextPref(String prefName) {
            if (debug) Timber.i("refreshEditTextPref");
            EditTextPreference pref = (EditTextPreference) findPreference(prefName);
            String newSummary = pref.getText();
            pref.setSummary(newSummary);
            return newSummary;
        }

        //-------------------------- OnSharedPreferenceChangeListener

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefName) {
            if (debug) Timber.i("onSharedPreferenceChanged prefName is %s", prefName);
            if (prefName == null || prefName.isEmpty()) {
                if (debug) Timber.e("onSharedPreferenceChanged prefName is illegal");
                return;
            }
            switch (prefName) {
                case SettingsDefault.TypeName.btLatencyMs:
                    PreferencesProcessor.saveBtLatencyMsPref(Integer.parseInt(getRefreshedEditTextPref(prefName)));
                    break;
                case SettingsDefault.TypeName.btChosenAddr:
                    PreferencesProcessor.saveBtChosenAddrPref(getRefreshedEditTextPref(prefName));
                    break;
                case SettingsDefault.TypeName.opMode:
                    PreferencesProcessor.saveOpModePref(EOpMode.valueOf(getRefreshedListPref(prefName)));
                    break;
                case SettingsDefault.TypeName.audioCodecType:
                    PreferencesProcessor.saveAudioCodecTypePref(EAudioCodecType.valueOf(getRefreshedListPref(prefName)));
                    break;
                case SettingsDefault.TypeName.bt2BtPacketSize:
                    PreferencesProcessor.saveBt2btPacketSizePref(Integer.parseInt(getRefreshedEditTextPref(prefName)));
                    break;
                case SettingsDefault.TypeName.btSinglePacket:
                    PreferencesProcessor.saveBtSinglePacketPref();
                default:
                    break;
            }
        }

    }

}
