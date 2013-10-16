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

import android.content.Context;
import android.content.Intent;

public abstract class Plugin {

    private Context       context;
    private WebSocketInfo info;

    abstract public void execute(Request request);

    public void setContext(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    public void setConnectionInfo(WebSocketInfo info) {
        this.info = info;
    }

    protected WebSocketInfo getConnectionInfo() {
        return info;
    }

    public void onCreate(String origin) {
        // Do nothing
    }

    public void onDestroy() {
        // Do nothing
    }

    protected void callActivity(Request request, Intent intent) {
        PluginManager.callActivityViaProxy(request, intent);
    }

    public void resultFromActivity(Request request, int resultCode, Intent data) {
        // Do nothing
    }

    protected void finishProxyActivity() {
        if (context instanceof ProxyActivity) {
            ((ProxyActivity) context).finish();
        }
    }

    protected void runInContextOfProxyActivity(Request request) {
        PluginManager.runViaProxy(request);
    }

    public void callbackFromProxy(Request request) {
        // Do nothing
    }
}
