package org.webappbooster;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;

public class PluginManager {

    static private Context                       context;
    private Map<String, Class<? extends Plugin>> plugins;

    public PluginManager(Context c) {
        context = c;
        plugins = new HashMap<String, Class<? extends Plugin>>();
        XmlResourceParser parser = context.getResources().getXml(R.xml.plugins);
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
                        String action = parser.getAttributeValue(null, "action");
                        plugins.put(action, clazz);
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

    public void dispatchRequest(int connectionId, String origin, String message) {
        try {
            JSONObject msg = new JSONObject(message);
            String action = msg.getString("action");
            Plugin instance = getPluginInstance(connectionId, origin, action);
            Class<?>[] args = new Class[] { JSONObject.class };
            Method meth = Plugin.class.getDeclaredMethod("execute", args);
            meth.invoke(instance, msg);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();

    private Plugin getPluginInstance(int connectionId, String origin, String action)
            throws InstantiationException, IllegalAccessException {
        Class<? extends Plugin> clazz = plugins.get(action);
        String key = clazz.getName() + "-" + connectionId;
        Plugin plugin = pluginMap.get(key);
        if (plugin == null) {
            // No plugin for this connection created yet
            plugin = clazz.newInstance();
            plugin.setConnectionId(connectionId);
            plugin.setContext(context);
            plugin.onCreate(origin);
            pluginMap.put(key, plugin);
        }
        return plugin;
    }

    static private int                  nextRequestId = 0;
    static private Map<Integer, Plugin> requestMap    = new HashMap<Integer, Plugin>();

    static public void callActivityViaProxy(Plugin caller, Intent intent) {
        int id = nextRequestId++;
        requestMap.put(id, caller);
        Intent i = new Intent(context, ProxyActivity.class);
        i.putExtra("ACTION", "CALL_ACTIVITY");
        i.putExtra("INTENT", intent);
        i.putExtra("ID", id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(i);
    }

    static public void resultFromActivity(int requestCode, int resultCode, Intent data) {
        Plugin caller = requestMap.get(requestCode);
        requestMap.remove(requestCode);
        caller.resultFromActivity(resultCode, data);
    }

    static public void runViaProxy(Plugin caller) {
        int id = nextRequestId++;
        requestMap.put(id, caller);
        Intent i = new Intent(context, ProxyActivity.class);
        i.putExtra("ACTION", "CALL_PLUGIN");
        i.putExtra("ID", id);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(i);
    }

    static public void runPlugin(Context proxy, int id) {
        Plugin caller = requestMap.get(id);
        requestMap.remove(id);
        caller.setContext(proxy);
        caller.callbackFromProxy();
    }

    public static void websocketClosed(int connectionId) {
        Set<String> keys = pluginMap.keySet();
        String suffix = "-" + connectionId;
        for (String key : keys) {
            if (key.endsWith(suffix)) {
                Plugin plugin = pluginMap.get(key);
                plugin.onDestroy();
                pluginMap.remove(key);
            }
        }
    }
}
