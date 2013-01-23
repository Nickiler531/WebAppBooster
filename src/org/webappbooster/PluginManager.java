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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class PluginManager {

    static private Map<String, Plugin>           pluginInstanceMap = new HashMap<String, Plugin>();
    static private int                           nextRequestId     = 0;
    static private Map<Integer, Request>         requestMap        = new HashMap<Integer, Request>();

    private Map<String, Class<? extends Plugin>> pluginClassMap;
    private Map<String, String>                  permissionMap;
    static private Map<String, String[]>         actionMap;

    public PluginManager() {
        pluginClassMap = new HashMap<String, Class<? extends Plugin>>();
        permissionMap = new HashMap<String, String>();
        actionMap = new HashMap<String, String[]>();
        XmlResourceParser parser = BoosterApplication.getAppContext().getResources()
                .getXml(R.xml.plugins);
        try {
            parser.next();
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("plugin")) {
                        String clazzName = parser.getAttributeValue(null, "class");
                        clazzName = "org.webappbooster.plugin." + clazzName;
                        Class<? extends Plugin> clazz = (Class<? extends Plugin>) Class
                                .forName(clazzName);
                        String[] actions = parser.getAttributeValue(null, "actions").split("\\|");
                        String permission = parser.getAttributeValue(null, "permission");
                        actionMap.put(permission, actions);
                        for (String action : actions) {
                            pluginClassMap.put(action, clazz);
                            if (permission != null) {
                                permissionMap.put(action, permission);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        parser.close();
    }

    public void dispatchRequest(WebSocketInfo info, String message) {
        try {
            int connectionId = info.getConnectionId();
            String origin = info.getOrigin();

            Request req = new Request(message);
            if (req.isRequestMalformed()) {
                Log.d("WAB", "Malformed request: " + message);
                return;
            }
            String action = req.getAction();
            int requestId = req.getRequestId();
            if (!info.isAuthenticated()
                    && !(action.equals("REQUEST_AUTHENTICATION") || action.equals("AUTHENTICATE"))) {
                // Connection has not yet been authenticated.
                sendError(connectionId, requestId, Response.NOT_AUTHORIZED);
                return;
            }
            if (!hasPermission(connectionId, origin, action)) {
                sendError(connectionId, requestId, Response.NOT_AUTHORIZED);
                return;
            }
            Plugin instance = getPluginInstance(info, origin, action);
            if (instance == null) {
                Log.d("WAB", "Cannot get plugin for request: " + message);
                return;
            }
            req.setManagingPlugin(instance);
            Class<?>[] args = new Class[] { Request.class };
            Method meth = Plugin.class.getDeclaredMethod("execute", args);
            meth.invoke(instance, req);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            try {
                throw e.getCause();
            } catch (Throwable ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendError(int connectionId, int requestId, int error) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("id", requestId);
        result.put("status", error);
        BoosterService.getService().sendResult(connectionId, result.toString());
    }

    private boolean hasPermission(int connectionId, String origin, String action) {
        String permission = permissionMap.get(action);
        if (permission == null) {
            return true;
        }
        return Authorization.checkOnePermission(origin, permission);
    }

    private Plugin getPluginInstance(WebSocketInfo info, String origin, String action) {
        int connectionId = info.getConnectionId();
        Class<? extends Plugin> clazz = pluginClassMap.get(action);
        String key = clazz.getName() + "-" + connectionId;
        Plugin plugin = pluginInstanceMap.get(key);
        if (plugin == null) {
            // No plugin for this connection created yet
            try {
                plugin = clazz.newInstance();
            } catch (InstantiationException e) {
                Log.d("WAB", "InstantiationException. origin=" + origin + ", action=" + action);
                return null;
            } catch (IllegalAccessException e) {
                Log.d("WAB", "IllegalAccessException. origin=" + origin + ", action=" + action);
                return null;
            }
            plugin.setConnectionInfo(info);
            plugin.setContext(BoosterApplication.getAppContext());
            plugin.onCreate(origin);
            pluginInstanceMap.put(key, plugin);
        }
        return plugin;
    }

    static public void callActivityViaProxy(Request request, Intent intent) {
        int id = nextRequestId++;
        Context context = BoosterApplication.getAppContext();
        requestMap.put(id, request);
        Intent i = new Intent(context, ProxyActivity.class);
        i.putExtra("ACTION", "CALL_ACTIVITY");
        i.putExtra("INTENT", intent);
        i.putExtra("ID", id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    static public void resultFromActivity(int requestCode, int resultCode, Intent data) {
        Request request = requestMap.get(requestCode);
        Plugin caller = request.getManagingPlugin();
        requestMap.remove(requestCode);
        caller.resultFromActivity(request, resultCode, data);
    }

    static public void runViaProxy(Request request) {
        int id = nextRequestId++;
        Context context = BoosterApplication.getAppContext();
        requestMap.put(id, request);
        Intent i = new Intent(context, ProxyActivity.class);
        i.putExtra("ACTION", "CALL_PLUGIN");
        i.putExtra("ID", id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    static public void runPlugin(Context proxy, int id) {
        Request request = requestMap.get(id);
        Plugin caller = request.getManagingPlugin();
        requestMap.remove(id);
        caller.setContext(proxy);
        caller.callbackFromProxy(request);
    }

    public static void websocketClosed(int connectionId) {
        Set<String> keys = new HashSet<String>(pluginInstanceMap.keySet());
        String suffix = "-" + connectionId;
        for (String key : keys) {
            if (key.endsWith(suffix)) {
                Plugin plugin = pluginInstanceMap.get(key);
                plugin.onDestroy();
                pluginInstanceMap.remove(key);
            }
        }
    }

    static public String[] getActionsForPermission(String permission) {
        return actionMap.get(permission);
    }
}
