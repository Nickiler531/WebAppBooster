package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.util.Log;

public class BoosterWebSocket extends WebSocketServer {

    private PluginManager                 pluginManager;

    private Map<WebSocket, WebSocketInfo> infoMap          = new HashMap<WebSocket, WebSocketInfo>();
    private List<String>                  originList       = new ArrayList<String>();
    private Map<Integer, WebSocket>       websocketMap     = new HashMap<Integer, WebSocket>();

    static private int                    nextConnectionId = 0;

    public BoosterWebSocket(PluginManager pluginManager, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.pluginManager = pluginManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (!conn.getRemoteSocketAddress().getAddress().getHostAddress().equals("127.0.0.1")) {
            Log.d("WAB", "Connection did not originate from localhost");
            conn.close(CloseFrame.GOING_AWAY);
            return;
        }
        String origin = handshake.getFieldValue("origin");
        if (originList.contains(origin)) {
            // There is already an open connection from this origin. Immediately
            // close it.
            Log.d("WAB", "Second connection from origin " + origin);
            conn.close(CloseFrame.GOING_AWAY);
            return;
        }
        int id = nextConnectionId++;
        WebSocketInfo connection = new WebSocketInfo(conn, id, origin);
        infoMap.put(conn, connection);
        websocketMap.put(id, conn);
        originList.add(origin);
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
            originList.remove(origin);
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
        return originList.toArray(new String[originList.size()]);
    }

    public void sendResult(int connectionId, String result) {
        WebSocket conn = websocketMap.get(connectionId);
        if (conn != null) {
            conn.send(result);
        }
    }
}
