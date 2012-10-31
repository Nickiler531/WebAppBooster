package org.webappbooster.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Authorization;
import org.webappbooster.PermissionsDialog;
import org.webappbooster.Plugin;

import android.content.DialogInterface;
import android.util.Log;

public class PermissionsPlugin extends Plugin {

    private JSONObject request;
    private String     origin;

    @Override
    public void onCreate(String origin) {
        this.origin = origin;
    }

    public void execute(JSONObject request) {
        this.request = request;
        runInContextOfProxyActivity();
    }

    @Override
    public void callbackFromProxy() {
        JSONArray permissions;
        try {
            permissions = request.getJSONArray("permissions");
            final String[] p = new String[permissions.length()];
            for (int i = 0; i < p.length; i++) {
                p[i] = permissions.getString(i);
            }
            if (Authorization.checkPermissions(origin, p)) {
                // Permissions were granted earlier
                JSONObject result = new JSONObject();
                result.put("permission_granted", true);
                sendResultAndExit(result);
                return;
            }

            // Open dialog to ask user
            PermissionsDialog w = new PermissionsDialog(getContext());
            w.requestPermissions(origin, p);
            w.show(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which != DialogInterface.BUTTON_NEGATIVE) {
                        Authorization.setPermissions(origin, p,
                                which == DialogInterface.BUTTON_NEUTRAL);
                    }
                    JSONObject result = new JSONObject();
                    try {
                        result.put("permission_granted", which != DialogInterface.BUTTON_NEGATIVE);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    sendResultAndExit(result);
                    Log.d("WAB", "Clicked: " + which);
                }
            });
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
