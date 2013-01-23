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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Request {

    private JSONObject msg;
    private String     action;
    private int        requestId;
    private Plugin     managingPlugin;
    private boolean    requestMalformed;

    public Request(String message) {
        managingPlugin = null;
        requestMalformed = false;
        try {
            msg = new JSONObject(message);
            action = msg.getString("action");
            requestId = msg.getInt("id");
        } catch (JSONException e) {
            msg = null;
            action = "";
            requestMalformed = true;
        }
    }

    public boolean isRequestMalformed() {
        return this.requestMalformed;
    }

    public Response createResponse(int code) {
        return new Response(code, requestId, managingPlugin);
    }

    public String getAction() {
        return action;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getString(String key) {
        try {
            return msg.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    public double getDouble(String key) {
        try {
            return msg.getDouble(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public int getInt(String key) {
        try {
            return msg.getInt(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    public String[] getStringArray(String key) {
        try {
            JSONArray a = msg.getJSONArray(key);
            String[] strings = new String[a.length()];
            for (int i = 0; i < a.length(); i++) {
                strings[i] = a.getString(i);
            }
            return strings;
        } catch (JSONException e) {
            return new String[0];
        }
    }

    public void setManagingPlugin(Plugin plugin) {
        this.managingPlugin = plugin;
    }

    public Plugin getManagingPlugin() {
        return this.managingPlugin;
    }
}
