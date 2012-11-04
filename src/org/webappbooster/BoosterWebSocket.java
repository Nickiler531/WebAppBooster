package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class BoosterWebSocket extends WebSocketServer {

    private PluginManager           pluginManager;

    private Map<WebSocket, String>  originMap        = new HashMap<WebSocket, String>();
    private Map<WebSocket, Integer> connectionIdMap  = new HashMap<WebSocket, Integer>();
    private Map<Integer, WebSocket> websocketMap     = new HashMap<Integer, WebSocket>();

    static private int              nextConnectionId = 0;

    public BoosterWebSocket(PluginManager pluginManager, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.pluginManager = pluginManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        int id = nextConnectionId++;
        String origin = handshake.getFieldValue("origin");
        originMap.put(conn, origin);
        connectionIdMap.put(conn, id);
        websocketMap.put(id, conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        int id = connectionIdMap.get(conn);
        PluginManager.websocketClosed(id);
        String origin = originMap.get(conn);
        Authorization.revokeOneTimePermissions(origin);
        originMap.remove(conn);
        connectionIdMap.remove(conn);
        websocketMap.remove(id);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        int id = connectionIdMap.get(conn);
        String origin = originMap.get(conn);
        pluginManager.dispatchRequest(id, origin, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    public String[] getOpenConnections() {
        return originMap.values().toArray(new String[originMap.size()]);
    }

    public void sendResult(int connectionId, String result) {
        WebSocket conn = websocketMap.get(connectionId);
        conn.send(result);
    }
}
