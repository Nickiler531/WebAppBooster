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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

public class CameraPlugin extends Plugin {

    final private static String PATH = "/WebAppBooster/";

    private int                 requestId;

    private File                file;

    @Override
    public void execute(int requestId, String action, JSONObject request) {
        this.requestId = requestId;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        file = new File(dir.getAbsolutePath() + "/camera-output");
        Uri fileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        callActivity(intent);
    }

    @Override
    public void resultFromActivity(int resultCode, Intent data) {
        /*
         * Unfortunately we cannot return the URI to the web app since Chrome
         * under Android does not permit access to a file:// for security
         * reasons (otherwise a web app could just read the content of the
         * sdcard).
         */
        JSONObject result = new JSONObject();
        try {
            if (resultCode != Activity.RESULT_OK) {
                result.put("status", -1); // TODO should be different error code
                                          // (Aborted)
            } else {
                byte[] image = FileUtils.readFileToByteArray(file);
                byte[] imageBase64 = Base64.encode(image, Base64.DEFAULT);
                result.put("status", 0);
                result.put("uri", "data:image/png;base64," + new String(imageBase64));
            }
            sendResult(requestId, result);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
