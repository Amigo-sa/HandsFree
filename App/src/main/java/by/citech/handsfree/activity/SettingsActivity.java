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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import by.citech.handsfree.R;
import by.citech.handsfree.parameters.Colors;
import by.citech.handsfree.settings.SettingsHelper;
import by.citech.handsfree.settings.Settings;
import by.citech.handsfree.settings.SettingsDefault;
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
            SettingsHelper.prepareListPref    ((ListPreference    ) findPreference(getString(R.string.opMode         )), Settings.Common.opMode                                                       );
            SettingsHelper.prepareListPref    ((ListPreference    ) findPreference(getString(R.string.audioCodecType )), Settings.AudioCommon.audioCodecType                                          );
            SettingsHelper.prepareEditTextPref((EditTextPreference) findPreference(getString(R.string.btLatencyMs    )), Settings.Bluetooth.btLatencyMs     , SettingsDefault.TypeName.btLatencyMs    );
            SettingsHelper.prepareEditTextPref((EditTextPreference) findPreference(getString(R.string.bt2NetFactor   )), Settings.Common.bt2NetFactor       , SettingsDefault.TypeName.bt2NetFactor   );
            SettingsHelper.prepareEditTextPref((EditTextPreference) findPreference(getString(R.string.bt2BtPacketSize)), Settings.Bluetooth.bt2BtPacketSize , SettingsDefault.TypeName.bt2BtPacketSize);
            SettingsHelper.prepareEditTextPref((EditTextPreference) findPreference(getString(R.string.btChosenAddr   )), Settings.Bluetooth.btChosenAddr    , SettingsDefault.TypeName.btChosenAddr   );
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
            try {
                return super.onCreateView(inflater, container, savedInstanceState);
            } finally {
                setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
            }
        }

        //-------------------------- on change refresh

        private String getRefreshedListPref(String prefName) {
            ListPreference pref = (ListPreference) findPreference(prefName);
            CharSequence newSummary = pref.getEntry();
            if (debug) Timber.w("getRefreshedListPref %s set to %s", prefName, newSummary);
            pref.setSummary(newSummary);
            return newSummary.toString();
        }

        private String getRefreshedEditTextPref(String prefName) {
            EditTextPreference pref = (EditTextPreference) findPreference(prefName);
            String newSummary = pref.getText();
            if (debug) Timber.w("getRefreshedEditTextPref %s set to %s", prefName, newSummary);
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
                case SettingsDefault.TypeName.bt2NetFactor:
                case SettingsDefault.TypeName.btLatencyMs:
                case SettingsDefault.TypeName.btChosenAddr:
                case SettingsDefault.TypeName.bt2BtPacketSize:
                    getRefreshedEditTextPref(prefName);
                    break;
                case SettingsDefault.TypeName.opMode:
                case SettingsDefault.TypeName.audioCodecType:
                    getRefreshedListPref(prefName);
                    break;
                case SettingsDefault.TypeName.btSinglePacket:
                default:
                    break;
            }
        }

    }

}
