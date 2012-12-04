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
            FileInputStream is = MainActivity.activity.openFileInput(FILE_NAME);
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
            FileOutputStream os = MainActivity.activity.openFileOutput(FILE_NAME,
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
