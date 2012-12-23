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

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.Request;
import org.webappbooster.WebSocketInfo;

import android.content.Intent;
import android.net.Uri;

public class AuthenticationPlugin extends Plugin {

    private Intent intent;

    @Override
    public void execute(Request request) throws JSONException {
        WebSocketInfo info = this.getConnectionInfo();
        String action = request.getAction();
        if (action.equals("REQUEST_AUTHENTICATION")) {
            String path = request.getString("path");
            path += "#webappbooster_token=" + info.getToken();
            String url = info.getOrigin() + "/" + path;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            info.closeConnection();
            runInContextOfProxyActivity(request);
        } else {
            double token = request.getDouble("token");
            int status = 0;
            if (token == info.getToken()) {
                info.connectionIsAuthorized();
            } else {
                status = -1;
            }
            JSONObject result = new JSONObject();
            result.put("status", status);
            sendResult(request.getRequestId(), result);
        }
    }

    @Override
    public void callbackFromProxy(Request request) throws JSONException {
        getContext().startActivity(intent);
        finishProxyActivity();
    }
}
