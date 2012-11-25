package org.webappbooster.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Authorization;
import org.webappbooster.PermissionsDialog;
import org.webappbooster.Plugin;

import android.content.DialogInterface;

public class PermissionsPlugin extends Plugin {

    private JSONObject request;
    private int        requestId;
    private String     origin;

    @Override
    public void onCreate(String origin) {
        this.origin = origin;
    }

    @Override
    public void execute(int requestId, String action, JSONObject request) {
        this.requestId = requestId;
        this.request = request;
        runInContextOfProxyActivity();
    }

    @Override
    public void callbackFromProxy() throws JSONException {
        JSONArray permissions = request.getJSONArray("permissions");
        final String[] p = new String[permissions.length()];
        for (int i = 0; i < p.length; i++) {
            p[i] = permissions.getString(i);
        }
        if (Authorization.checkPermissions(origin, p)) {
            // Permissions were granted earlier
            JSONObject result = new JSONObject();
            result.put("status", 0);
            sendResult(requestId, result);
            return;
        }

        // Open dialog to ask user
        PermissionsDialog w = new PermissionsDialog(getContext());
        w.requestPermissions(origin, p);
        w.show(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != DialogInterface.BUTTON_NEGATIVE) {
                    Authorization
                            .setPermissions(origin, p, which == DialogInterface.BUTTON_NEUTRAL);
                }
                JSONObject result = new JSONObject();
                try {
                    result.put("status", (which != DialogInterface.BUTTON_NEGATIVE) ? 0 : -1);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sendResult(requestId, result);
            }
        });
    }

}
