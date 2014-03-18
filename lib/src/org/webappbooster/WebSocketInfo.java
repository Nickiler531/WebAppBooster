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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;

import android.content.Context;

public class WebSocketInfo {

    final static private String        FILE_NAME = "webappbooster-tokens";

    private static Map<String, Double> tokenMap;

    private WebSocket                  webSocket;
    private int                        connectionId;
    private String                     origin;
    private boolean                    isAuthenticated;

    static {
        readTokens();
    }

    public WebSocketInfo(WebSocket webSocket, int connectionId, String origin) {
        this.webSocket = webSocket;
        this.connectionId = connectionId;
        this.origin = origin;
        if (!tokenMap.containsKey(origin)) {
            tokenMap.put(origin, Math.random());
            writeTokens();
        }
        this.isAuthenticated = false;
    }

    static private void readTokens() {
        tokenMap = new HashMap<String, Double>();
        try {
            FileInputStream is = BoosterApplication.getAppContext().openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(is);
            tokenMap = (Map<String, Double>) ois.readObject();
            ois.close();
            is.close();
        } catch (FileNotFoundException e) {
        } catch (StreamCorruptedException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    static private void writeTokens() {
        try {
            FileOutputStream os = BoosterApplication.getAppContext().openFileOutput(FILE_NAME,
                    Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(tokenMap);
            oos.close();
            os.close();
        } catch (FileNotFoundException e) {
        } catch (StreamCorruptedException e) {
        } catch (IOException e) {
        }
    }

    public static boolean isValidToken(double token) {
        return tokenMap.values().contains(token);
    }

    public int getConnectionId() {
        return this.connectionId;
    }

    public String getOrigin() {
        return this.origin;
    }

    public double getToken() {
        return tokenMap.get(origin);
    }

    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    public void connectionIsAuthorized() {
        this.isAuthenticated = true;
    }

    public void closeConnection() {
        this.webSocket.close(CloseFrame.NORMAL);
    }
}
