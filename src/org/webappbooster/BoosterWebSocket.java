package org.webappbooster;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;

import android.content.Intent;

public class BoosterWebSocket extends WebSocketServer {

    public static BoosterWebSocket singleton;
    
    public BoosterWebSocket(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        singleton = this;
    }

    public BoosterWebSocket(InetSocketAddress address) {
        super(address);
        singleton = this;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
            ClientHandshake request) throws InvalidDataException {
        System.out.println("NEW CONNECTION REQUEST");

        Iterator<String> fields = request.iterateHttpFields();
        while (fields.hasNext()) {
            String field = fields.next();
            System.out.println(field + ": " + request.getFieldValue(field));
        }
        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.sendToAll("new connection: " + handshake.getResourceDescriptor());
        System.out.println(conn.getRemoteSocketAddress().getAddress().getHostAddress()
                + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.sendToAll(conn + " has left the room!");
        System.out.println(conn + " has left the room!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Intent intent = new Intent(MainActivity.activity, ProxyActivity.class);
        intent.putExtra("ACTION", ProxyActivity.PICK_CONTACT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity.activity.startActivity(intent);

        this.sendToAll(message);
        System.out.println(conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
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
}
