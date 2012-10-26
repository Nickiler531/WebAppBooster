package org.webappbooster;

import android.content.Intent;
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
            Preference enableWAB = findPreference(key);
            if (enableWAB.isEnabled()) {
            	startBoosterService();
            }
            else {
            	stopBoosterService();
            }
        }
    }

    private void startBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.startService(intent);
    }

    private void stopBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.stopService(intent);
    }
}