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
import org.webappbooster.R;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.Activity;
import android.content.Intent;

public class SharePlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("SHARE")) {
            executeShare(request);
        }
    }

    private void executeShare(Request request) {
        Intent intent = null;

        String text = request.getString("text");
        if (!"".equals(text)) {
            intent = createShareTextOrUriIntent(text);
        }

        String uri = request.getString("uri");
        if (!"".equals(uri)) {
            intent = createShareTextOrUriIntent(uri);
        }

        String photoUri = request.getString("photoUri");
        if (!"".equals(photoUri)) {
            intent = createSharePhotoIntent(photoUri);
        }

        if (intent == null) {
            Response response = request.createResponse(Response.ERR_MALFORMED_REQUEST);
            response.send();
        } else {
            callActivity(request,
                    Intent.createChooser(intent, getContext().getString(R.string.share_options)));
        }
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        Response response = request.createResponse(resultCode == Activity.RESULT_OK ? Response.OK
                : Response.ERR_CANCELLED);
        response.send();
    }

    private Intent createShareTextOrUriIntent(String text) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    private Intent createSharePhotoIntent(String uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        return intent;
    }
}
