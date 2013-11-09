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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.webappbooster.Authorization;
import org.webappbooster.MainActivity;
import org.webappbooster.PermissionsDialog;
import org.webappbooster.Plugin;
import org.webappbooster.PluginManager;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.content.DialogInterface;
import android.util.Log;

@PluginMappingAnnotation(actions = "REQUEST_PERMISSIONS", permission = "")
public class PermissionsPlugin extends Plugin {

    private String   origin;
    private String[] permissions;

    @Override
    public void onCreate(String origin) {
        this.origin = origin;
    }

    @Override
    public void execute(Request request) {
        runInContextOfProxyActivity(request);
    }

    @Override
    public void callbackFromProxy(final Request request) {
        permissions = request.getStringArray("permissions");
        if (permissions == null) {
            Log.d("WAB", "PermissionsPlugin: request does not contain permissions");
            sendStatus(request, Response.ERR_MALFORMED_REQUEST);
            return;
        }
        if (Authorization.checkPermissions(origin, permissions)) {
            // Permissions were granted earlier
            sendStatus(request, Response.OK);
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
                sendStatus(request, (which != DialogInterface.BUTTON_NEGATIVE) ? Response.OK
                        : Response.ERR_PERMISSION_DENIED);
            }
        });
    }

    private void sendStatus(Request request, int status) {
        Response response = request.createResponse(status);
        response.add("version", MainActivity.VERSION);
        if (status == Response.OK) {
            // Get all supported actions for the requested permissions
            List<String> supportedActions = new ArrayList<String>();
            supportedActions.addAll(PluginManager.getActionsWithoutPermissions());
            for (String p : permissions) {
                String[] actions = PluginManager.getActionsForPermission(p);
                if (actions != null) {
                    supportedActions.addAll(Arrays.asList(actions));
                }
            }
            response.add("supportedActions", supportedActions);
        }
        response.send();
    }
}
