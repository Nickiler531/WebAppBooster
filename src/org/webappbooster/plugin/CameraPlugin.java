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

import java.io.File;

import org.webappbooster.HTTPServer;
import org.webappbooster.Plugin;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class CameraPlugin extends Plugin {

    final private static String PATH = "/WebAppBooster/";

    private String              path;

    @Override
    public void execute(Request request) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path = dir.getAbsolutePath() + "/camera-output";
        File file = new File(path);
        Uri fileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        callActivity(request, intent);
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        /*
         * Unfortunately we cannot return the URI to the web app since Chrome
         * under Android does not permit access to a file:// for security
         * reasons (otherwise a web app could just read the content of the
         * sdcard).
         */
        Response response;
        if (resultCode != Activity.RESULT_OK) {
            response = request.createResponse(Response.ERR_CANCELLED);
        } else {
            response = request.createResponse(Response.OK);
            String uri = HTTPServer.genResourceUri(this.getConnectionInfo().getToken(), "file://"
                    + path, "image/png");
            response.add("uri", uri);
        }
        response.send();
    }
}
