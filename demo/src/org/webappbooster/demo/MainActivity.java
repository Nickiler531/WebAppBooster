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

package org.webappbooster.demo;

import org.webappbooster.BoosterService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

public class MainActivity extends Activity implements ServiceConnection {

    final static String DEMO_URL = "http://webappbooster.org/demo/packaged";

    private double      token;

    private String constructURL(int port) {
        String url = DEMO_URL;
        url += "#webappbooster_token=" + token;
        url += "|port=" + port;
        return url;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder localBinder) {
        BoosterService.LocalBinder binder = (BoosterService.LocalBinder) localBinder;
        BoosterService service = binder.getService();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(constructURL(service.getWebSocketPort())));
        startActivity(i);
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        // Do nothing
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, BoosterService.class);
        token = Math.random();
        intent.putExtra(BoosterService.PARAM_TOKEN, token);
        startService(intent);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
        Intent intent = new Intent(this, BoosterService.class);
        this.stopService(intent);
    }
}
