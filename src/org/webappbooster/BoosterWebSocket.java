package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.content.Intent;
import android.util.Log;

public class BoosterWebSocket extends WebSocketServer {

    private BoosterService          service;

    private Map<WebSocket, String>  originMap = new HashMap<WebSocket, String>();
    private Map<WebSocket, Integer> idMap     = new HashMap<WebSocket, Integer>();

    static private int              id        = 0;

    public BoosterWebSocket(BoosterService service, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.service = service;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String origin = handshake.getFieldValue("origin");
        originMap.put(conn, origin);
        idMap.put(conn, id++);
        this.sendToAll("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress()
                + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        originMap.remove(conn);
        idMap.remove(conn);
        this.sendToAll(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String origin = originMap.get(conn);
        Log.d("TAG", "Got request from: " + origin);
        Intent intent = new Intent(MainActivity.activity, ProxyActivity.class);
        intent.putExtra("ACTION", ProxyActivity.PICK_CONTACT);
        intent.putExtra("ID", idMap.get(conn));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        service.startActivity(intent);

        this.sendToAll(message);
        System.out.println(conn + ": " + message);
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
        // TODO Auto-generated method stub
        sendToAll(result);
    }
}
