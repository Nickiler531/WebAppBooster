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
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.Activity;
import android.content.Intent;

public class OAuthPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("OAUTH_AUTHENTICATION")) {
            executeOAuthAuthentication(request);
        }
    }

    private void executeOAuthAuthentication(Request request) {
        String uri = request.getString("uri");
        String redirectUri = request.getString("redirectUri");
        Intent intent = new Intent(getContext(), OAuthActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("redirectUri", redirectUri);
        callActivity(request, intent);
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        Response response;
        if (resultCode != Activity.RESULT_OK) {
            response = request.createResponse(Response.CANCELLED);
        } else {
            response = request.createResponse(Response.OK);
            String uri = data.getExtras().getString("uri");
            response.add("uri", uri);
        }
        response.send();
    }
}
