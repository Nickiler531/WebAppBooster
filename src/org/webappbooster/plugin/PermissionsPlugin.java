package org.webappbooster.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Authorization;
import org.webappbooster.MainActivity;
import org.webappbooster.PermissionsDialog;
import org.webappbooster.Plugin;
import org.webappbooster.PluginManager;

import android.content.DialogInterface;

public class PermissionsPlugin extends Plugin {

    private JSONObject request;
    private int        requestId;
    private String     origin;
    private String[]   permissions;

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
        JSONArray p = request.getJSONArray("permissions");
        permissions = new String[p.length()];
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = p.getString(i);
        }
        if (Authorization.checkPermissions(origin, permissions)) {
            // Permissions were granted earlier
            sendStatus(0);
            return;
        }

        // Open dialog to ask user
        PermissionsDialog w = new PermissionsDialog(getContext());
        w.requestPermissions(origin, permissions);
        w.show(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != DialogInterface.BUTTON_NEGATIVE) {
                    Authorization.setPermissions(origin, permissions,
                            which == DialogInterface.BUTTON_NEUTRAL);
                }
                sendStatus((which != DialogInterface.BUTTON_NEGATIVE) ? 0 : -1);
            }
        });
    }

    private void sendStatus(int status) {
        JSONObject result = new JSONObject();
        try {
            result.put("status", status);
            result.put("version", MainActivity.VERSION);
            if (status == 0) {
                // Get all supported actions for the requested permissions
                List<String> supportedActions = new ArrayList<String>();
                for (String p : permissions) {
                    String[] actions = PluginManager.getActionsForPermission(p);
                    supportedActions.addAll(Arrays.asList(actions));
                }
                result.put("supportedActions", new JSONArray(supportedActions));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sendResult(requestId, result);

    }
}
