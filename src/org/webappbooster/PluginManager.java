package org.webappbooster;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

public class PluginManager {

    private Map<String, Class<? extends Plugin>> plugins;


    public PluginManager(Context context) {
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

    public void dispatchRequest(String action, String message) {
        Class<? extends Plugin> clazz = plugins.get(action);
        Plugin instance;
        try {
            instance = clazz.newInstance();
            Class<?>[] args = new Class[] { String.class };
            Method meth = clazz.getDeclaredMethod("execute", args);
            meth.invoke(instance, (Object[]) new String[] { message });
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
        }
    }
}
