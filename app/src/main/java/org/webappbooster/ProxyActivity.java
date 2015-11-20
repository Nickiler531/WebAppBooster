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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ProxyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String action = extras.getString("ACTION");
        int id = extras.getInt("ID");
        if (action.equals("CALL_ACTIVITY")) {
            Intent intent = extras.getParcelable("INTENT");
            startActivityForResult(intent, id);
        }
        if (action.equals("CALL_PLUGIN")) {
            PluginManager.runPlugin(this, id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PluginManager.resultFromActivity(requestCode, resultCode, data);
        finish();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("WAB", "ProxyActivity.onPause()");
    }
}
