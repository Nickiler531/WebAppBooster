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

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoosterWebSocket extends WebSocketServer {

    private BoosterService                service;

    private Map<WebSocket, WebSocketInfo> infoMap          = new HashMap<WebSocket, WebSocketInfo>();
    private List<String>                  originList       = new ArrayList<String>();
    private Map<Integer, WebSocket>       websocketMap     = new HashMap<Integer, WebSocket>();

    static private int                    nextConnectionId = 0;

    public BoosterWebSocket(BoosterService service) throws UnknownHostException {
        super(new InetSocketAddress(Config.PORT_WEBSOCKET));
        this.service = service;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String originAddr = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        if (!"127.0.0.1".equals(originAddr) && !"::1".equals(originAddr)) {
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
        service.updateNotification();
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
            service.updateNotification();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WebSocketInfo info = infoMap.get(conn);
        if (info == null) {
            Log.d("WAB", "BoosterWebSocket.onMessage(): info == null");
            return;
        }
        service.getPluginManager().dispatchRequest(info, message);
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
