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

import android.content.Context;
import android.os.Vibrator;


@PluginMappingAnnotation(actions = "VIBRATE", permission = "VIBRATE")
public class VibratePlugin extends Plugin {

    private Vibrator vibrator;

    @Override
    public void onCreate(String origin) {
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void execute(Request request) {
        int millis = request.getInt("millis");
        vibrator.vibrate(millis);
        Response response = request.createResponse(Response.OK);
        response.send();
    }
}
