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

package org.webappbooster.plugin;

import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@PluginMappingAnnotation(actions = "NETWORK_INFORMATION", permission = "")
public class ConnectivityPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("NETWORK_INFORMATION")) {
            ConnectivityManager cm = (ConnectivityManager) this.getContext().getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            String ct = "unknown";
            if (ni == null || !ni.isConnected()) {
                ct = "none";
            } else {
                switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    ct = "wifi";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    ct = "ethernet";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    ct = "mobile";
                    break;
                default:
                    // TouchDevelop does not know about other connection types
                }
            }
            Response response = request.createResponse(Response.OK);
            response.add("connectionType", ct);
            response.send();
        }
    }

}
