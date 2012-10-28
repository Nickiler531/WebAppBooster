package org.webappbooster;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    public static final String PREF_KEY_ENABLE_WAB         = "pref_key_enable_wab";
    public static final String PREF_KEY_ENABLE_WAB_ON_BOOT = "pref_key_enable_wab_on_boot";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}