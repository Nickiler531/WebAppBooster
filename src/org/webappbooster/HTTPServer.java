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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import android.util.Log;

public class HTTPServer implements Container {

    static public class Resource {
        public Resource(String path, String mimeType) {
            this.path = path;
            this.mimeType = mimeType;
        }

        public String path;
        public String mimeType;
    }

    private static Map<String, Resource> resources = new HashMap<String, Resource>();

    public static void addResource(String resourceId, Resource resource) {
        resources.put(resourceId, resource);
    }

    public void handle(Request request, Response response) {
        PrintStream body = null;
        String resourceId = null;
        try {
            body = response.getPrintStream();
            long time = System.currentTimeMillis();

            resourceId = request.getPath().getPath().substring(1);
            Log.d("WAB-HTTP", resourceId);
            Resource resource = resources.get(resourceId);
            if (resource == null) {
                throw new IOException();
            }
            resources.remove(resourceId);
            response.set("Content-Type", resource.mimeType);
            response.set("Server", "WebAppBooster/1.0");
            // CORS header is not needed since we will never send JavaScript
            // response.set("Access-Control-Allow-Origin", "*");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            FileUtils.copyFile(new File(resource.path), body);
            body.close();
        } catch (IOException e) {
            Log.d("WAB", "Cannot load resourceId: " + resourceId);
            response.setCode(404);
            body.close();
        }
    }
}