package org.webappbooster;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;

public class WebSocketInfo {

    private static Map<String, Double> tokenMap = new HashMap<String, Double>();

    private WebSocket                  webSocket;
    private int                        connectionId;
    private String                     origin;
    private boolean                    isAuthenticated;

    public WebSocketInfo(WebSocket webSocket, int connectionId, String origin) {
        this.webSocket = webSocket;
        this.connectionId = connectionId;
        this.origin = origin;
        if (!tokenMap.containsKey(origin)) {
            tokenMap.put(origin, Math.random());
        }
        this.isAuthenticated = false;
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
