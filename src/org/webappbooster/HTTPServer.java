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

package org.webappbooster;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import android.net.Uri;
import android.util.Log;

public class HTTPServer implements Container {

    public static String genResourceUri(double token, String resourceUri, String mimeType) {
        Uri.Builder b = Uri.parse("http://localhost:" + Config.PORT_HTTP).buildUpon();
        b.appendQueryParameter("token", Double.toString(token));
        b.appendQueryParameter("uri", resourceUri);
        b.appendQueryParameter("mimeType", mimeType);
        return b.build().toString();
    }

    public void handle(Request request, Response response) {
        PrintStream body = null;
        try {
            body = response.getPrintStream();
            long time = System.currentTimeMillis();

            String token = request.getParameter("token");
            String uri = request.getParameter("uri");
            String mimeType = request.getParameter("mimeType");

            if (token == null || uri == null || mimeType == null) {
                throw new IOException();
            }

            if (!WebSocketInfo.isValidToken(Double.parseDouble(token))) {
                throw new IOException();
            }

            Log.d("WAB-HTTP", uri);
            response.set("Content-Type", mimeType);
            response.set("Server", "WebAppBooster/1.0");
            // CORS header is not needed since we will never send JavaScript
            // response.set("Access-Control-Allow-Origin", "*");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            if (uri.startsWith("file://")) {
                FileUtils.copyFile(new File(uri.substring("file://".length())), body);
            }
            if (uri.startsWith("content://")) {
                IOUtils.copy(BoosterApplication.getAppContext().getContentResolver()
                        .openInputStream(Uri.parse(uri)), body);
            }
            body.close();
        } catch (IOException e) {
            Log.d("WAB", "Cannot load resource: " + request.getPath().getPath());
            response.setCode(404);
            body.close();
        }
    }
}