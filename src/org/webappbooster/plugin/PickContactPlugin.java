package org.webappbooster.plugin;

import org.webappbooster.Plugin;

import android.util.Log;

public class PickContactPlugin extends Plugin {
    
    public void execute(String request) {
        Log.d("WAB", "PickContactPlugin: " + request);
    }

}
