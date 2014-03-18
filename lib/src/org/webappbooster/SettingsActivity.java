/*
 * Copyright 2012-2013, webappbooster.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webappbooster;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import org.webappbooster.lib.R;

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