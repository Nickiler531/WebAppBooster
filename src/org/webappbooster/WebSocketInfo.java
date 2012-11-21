package org.webappbooster;

import org.java_websocket.WebSocket;

class WebSocketInfo {
    private WebSocket webSocket;
    private int       connectionId;
    private String    origin;


    public WebSocketInfo(WebSocket webSocket, int connectionId, String origin) {
        this.webSocket = webSocket;
        this.connectionId = connectionId;
        this.origin = origin;
    }

    public int getConnectionId() {
        return this.connectionId;
    }

    public String getOrigin() {
        return this.origin;
    }
}
