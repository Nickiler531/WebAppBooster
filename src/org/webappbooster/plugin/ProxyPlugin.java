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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.java_websocket.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.Request;
import org.webappbooster.Response;

public class ProxyPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("PROXY")) {
            executeProxy(request);
        }
    }

    private void executeProxy(final Request request) {
        final HttpURLConnection connection = createConnection(request);
        if (connection == null) {
            Response resp = request.createResponse(Response.ERR_MALFORMED_REQUEST);
            resp.send();
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                doHTTPRequest(connection, request);
            }
        }).start();
    }

    private HttpURLConnection createConnection(Request request) {
        String content = request.getStringOrNull("content");
        String contentText = request.getStringOrNull("contentText");
        if (content != null && contentText != null) {
            return null;
        }

        byte[] data = null;

        if (contentText != null) {
            data = contentText.getBytes();
        }

        if (content != null) {
            try {
                data = Base64.decode(content);
            } catch (IOException e) {
                return null;
            }
        }

        HttpURLConnection connection;
        String url = request.getString("url");
        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
        try {
            connection = (HttpURLConnection) u.openConnection();
        } catch (IOException e) {
            return null;
        }
        connection.setDoInput(true);

        // Method
        String method = request.getStringOrNull("method");
        if (method != null) {
            try {
                connection.setRequestMethod(method);
            } catch (ProtocolException e) {
                connection.disconnect();
                return null;
            }
        }

        // Headers
        JSONArray headersJSON = request.getJSONArray("headers");
        if (headersJSON != null) {
            String[] ignore = { "CONTENT-LENGTH", "ACCEPT-ENCODING", "ACCEPT-CHARSET",
                    "CONNECTION", "CONTENT-TRANSFER-ENCODING", "UPGRADE" };

            for (int i = 0; i < headersJSON.length(); i++) {
                JSONObject p;
                L: try {
                    p = headersJSON.getJSONObject(i);
                    String name = p.getString("name");
                    String value = p.getString("value");
                    for (int j = 0; j < ignore.length; j++) {
                        if (name.toUpperCase().equals(ignore[j])) {
                            break L;
                        }
                    }
                    connection.setRequestProperty(name, value);
                } catch (JSONException e) {
                    connection.disconnect();
                    return null;
                }
            }
        }

        // Credentials
        JSONObject credentialsJSON = request.getJSONObject("credentials");
        if (credentialsJSON != null) {
            try {
                final String name = credentialsJSON.getString("name");
                final String password = credentialsJSON.getString("password");
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(name, password.toCharArray());
                    }
                });
            } catch (JSONException e) {
                connection.disconnect();
                return null;
            }
        } else {
            Authenticator.setDefault(null);
        }

        if (data != null) {
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            try {
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                out.write(contentText.getBytes());
            } catch (IOException e) {
                connection.disconnect();
                return null;
            }
        }

        connection.setInstanceFollowRedirects(true);

        return connection;
    }

    private void doHTTPRequest(HttpURLConnection connection, Request request) {
        Response response = request.createResponse(Response.OK);
        String responseType = request.getString("responseType");
        try {
            // Response code
            response.add("code", connection.getResponseCode());

            // Header
            Map<String, List<String>> headers = connection.getHeaderFields();
            JSONArray headersJSON = new JSONArray();
            for (String key : headers.keySet()) {
                if (key == null || key.toUpperCase().equals("CONTENT-ENCODING")
                        || key.toUpperCase().equals("CONTENT-LENGTH")) {
                    continue;
                }
                List<String> value = headers.get(key);
                try {
                    JSONObject header = new JSONObject();
                    header.put("name", key);
                    header.put("value", value.get(0));
                    headersJSON.put(header);
                } catch (JSONException e) {
                }
            }
            response.add("headers", headersJSON);

            // Input
            InputStream in = new BufferedInputStream(connection.getInputStream());
            byte[] data = IOUtils.toByteArray(in);
            if (responseType.equals("text")) {
                response.add("contentText", new String(data));
            }
            if (responseType.equals("base64")) {
                response.add("contentBase64", Base64.encodeBytes(data));
            }
        } catch (IOException e) {
            response = request.createResponse(Response.ERR_CANCELLED);
        }
        connection.disconnect();
        Authenticator.setDefault(null);
        response.send();
    }
}
