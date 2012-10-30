package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.util.Log;

public class BoosterWebSocket extends WebSocketServer {

    private PluginManager           pluginManager;

    private Map<WebSocket, String>  originMap = new HashMap<WebSocket, String>();
    private Map<WebSocket, Integer> idMap     = new HashMap<WebSocket, Integer>();

    static private int              id        = 0;

    public BoosterWebSocket(PluginManager pluginManager, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.pluginManager = pluginManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String origin = handshake.getFieldValue("origin");
        originMap.put(conn, origin);
        idMap.put(conn, id++);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        originMap.remove(conn);
        idMap.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String origin = originMap.get(conn);
        Log.d("WAB", "Got request from: " + origin);
        pluginManager.dispatchRequest(origin, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    public String[] getOpenConnections() {
        return originMap.values().toArray(new String[originMap.size()]);
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     * 
     * @param text
     *            The String to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        Set<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

    public void resultFromProxy(int id, String result) {
        // WebSocket sock = idMap.get(id);
        // TODO Auto-generated method stub
        Log.d("WAB", "Result: " + result);
        sendToAll(result);
    }
}
