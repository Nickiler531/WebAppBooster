package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.util.Log;

public class BoosterWebSocket extends WebSocketServer {

    private PluginManager                 pluginManager;

    private Map<WebSocket, WebSocketInfo> infoMap          = new HashMap<WebSocket, WebSocketInfo>();
    private Map<String, Double>           tokenMap         = new HashMap<String, Double>();
    private Map<Integer, WebSocket>       websocketMap     = new HashMap<Integer, WebSocket>();

    static private int                    nextConnectionId = 0;


    public BoosterWebSocket(PluginManager pluginManager, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.pluginManager = pluginManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String origin = handshake.getFieldValue("origin");
        Double token = tokenMap.get(origin);
        if (token != null) {
            // There is already an open connection from this origin. Immediately
            // close it.
            Log.d("WAB", "Second connection from origin " + origin);
            conn.close(-1);
            return;
        }
        int id = nextConnectionId++;
        WebSocketInfo connection = new WebSocketInfo(conn, id, origin);
        infoMap.put(conn, connection);
        websocketMap.put(id, conn);
        tokenMap.put(origin, Math.random());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        WebSocketInfo info = infoMap.get(conn);
        if (info != null) {
            int id = info.getConnectionId();
            PluginManager.websocketClosed(id);
            String origin = info.getOrigin();
            Authorization.revokeOneTimePermissions(origin);
            infoMap.remove(conn);
            websocketMap.remove(id);
            tokenMap.remove(origin);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WebSocketInfo info = infoMap.get(conn);
        pluginManager.dispatchRequest(info, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    public String[] getOpenConnections() {
        return tokenMap.values().toArray(new String[tokenMap.size()]);
    }

    public void sendResult(int connectionId, String result) {
        WebSocket conn = websocketMap.get(connectionId);
        conn.send(result);
    }
}
