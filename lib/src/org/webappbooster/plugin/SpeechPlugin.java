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

import android.app.Activity;
import android.content.Intent;


@PluginMappingAnnotation(actions = "DICTATE", permission = "")
public class SpeechPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("DICTATE")) {
            executeDictate(request);
        }
    }

    private void executeDictate(Request request) {
        /*
         * Ordinarily we would fire the RecognizerIntent directly via the
         * ProxyActivity. However, since RecognizerIntent is not a proper
         * Activity, it is not rendered in time via ProxyActivity. That is why
         * we first launch DictActivity to ensure a UI is rendered so that the
         * RecognizerIntent immediately can show its own UI.
         */
        Intent intent = new Intent(this.getContext(), DictActivity.class);
        String caption = request.getString("caption");
        intent.putExtra("caption", caption);
        callActivity(request, intent);
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        Response response;
        if (resultCode != Activity.RESULT_OK) {
            response = request.createResponse(Response.ERR_CANCELLED);
        } else {
            response = request.createResponse(Response.OK);
            String text = data.getStringExtra("text");
            response.add("text", text);
        }
        response.send();
    }
}
