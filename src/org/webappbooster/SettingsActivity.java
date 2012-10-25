package org.webappbooster;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private static final String PREF_KEY_ENABLE_WAB         = "pref_key_enable_wab";
    private static final String PREF_KEY_ENABLE_WAB_ON_BOOT = "pref_key_enable_wab_on_boot";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_ENABLE_WAB)) {
            @SuppressWarnings("deprecation")
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

}