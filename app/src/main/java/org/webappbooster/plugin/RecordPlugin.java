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

import org.webappbooster.HTTPServer;
import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.Activity;
import android.content.Intent;


@PluginMappingAnnotation(actions = "RECORD_MICROPHONE", permission = "RECORD_AUDIO")
public class RecordPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("RECORD_MICROPHONE")) {
            executeRecordMicrophone(request);
        }
    }

    private void executeRecordMicrophone(Request request) {
        Intent intent = new Intent(this.getContext(), RecordActivity.class);
        callActivity(request, intent);
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        Response response;
        if (resultCode != Activity.RESULT_OK) {
            response = request.createResponse(Response.ERR_CANCELLED);
        } else {
            response = request.createResponse(Response.OK);
            String path = data.getStringExtra("path");
            String uri = HTTPServer.genResourceUri(this.getConnectionInfo().getToken(), "file://"
                    + path, "audio/wav");
            response.add("uri", uri);
        }
        response.send();
    }
}
