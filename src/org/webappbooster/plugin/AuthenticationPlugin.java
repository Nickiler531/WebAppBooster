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

import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;
import org.webappbooster.WebSocketInfo;

import android.content.Intent;
import android.net.Uri;
@PluginMappingAnnotation(actions="REQUEST_AUTHENTICATION|AUTHENTICATE",permission="")
public class AuthenticationPlugin extends Plugin {

    private Intent intent;

    @Override
    public void execute(Request request) {
        WebSocketInfo info = this.getConnectionInfo();
        String action = request.getAction();
        if (action.equals("REQUEST_AUTHENTICATION")) {
            String path = request.getString("path");
            path += "#webappbooster_token=" + info.getToken();
            String url = info.getOrigin() + "/" + path;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            info.closeConnection();
            getContext().startActivity(intent);
        } else {
            double token = request.getDouble("token");
            int status = Response.OK;
            if (token == info.getToken()) {
                info.connectionIsAuthorized();
            } else {
                status = Response.ERR_PERMISSION_DENIED;
            }
            Response response = request.createResponse(status);
            response.send();
        }
    }
}
