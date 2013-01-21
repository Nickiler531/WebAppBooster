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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {

    final public static int OK = 0;
    final public static int CANCELLED = -1;

    private JSONObject      response;
    private Plugin          managingPlugin;

    public Response(int code, int requestId, Plugin plugin) {
        managingPlugin = plugin;
        response = new JSONObject();
        try {
            response.put("id", requestId);
            response.put("status", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void send() {
        BoosterService.getService().sendResult(
                managingPlugin.getConnectionInfo().getConnectionId(), response.toString());
        managingPlugin.finishProxyActivity();
    }

    public void add(String name, String value) {
        if (value == null) {
            return;
        }
        try {
            response.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void add(String name, List<String> value) {
        try {
            response.put(name, new JSONArray(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void add(String name, int value) {
        try {
            response.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void add(String name, double value) {
        try {
            response.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
