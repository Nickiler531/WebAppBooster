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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BoosterService extends Service {

    private IBinder               binder           = new LocalBinder();
    private BoosterWebSocket      webSocket        = null;
    private Connection            httpSocket       = null;
    private Thread                httpServerThread = null;

    static private BoosterService service          = null;

    private PluginManager         pluginManager;

    class LocalBinder extends Binder {
        BoosterService getService() {
            return BoosterService.this;
        }
    }

    @Override
    public void onCreate() {
        service = this;
        pluginManager = new PluginManager();
        Log.d("WAB", "Starting WebAppBooster service");
        startWebSocketServer();
        startHttpServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        service = null;
        Log.d("WAB", "Stopping WebAppBooster service");
        stopWebSocketServer();
        stopHttpServer();
    }

    private void startWebSocketServer() {
        // WebSocket.DEBUG = true;
        try {
            if (webSocket == null) {
                webSocket = new BoosterWebSocket(pluginManager, Config.PORT_WEBSOCKET);
                webSocket.start();
            }
        } catch (UnknownHostException e) {
            webSocket = null;
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopWebSocketServer() {
        if (webSocket != null) {
            try {
                webSocket.stop();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
            webSocket = null;
        }
    }

    private void startHttpServer() {
        if (httpServerThread != null) {
            return;
        }
        httpServerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Container container = new HTTPServer();
                    httpSocket = new SocketConnection(container);
                    SocketAddress address = new InetSocketAddress(Config.PORT_HTTP);
                    httpSocket.connect(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        httpServerThread.start();
    }

    private void stopHttpServer() {
        try {
            httpSocket.close();
            httpSocket = null;
            httpServerThread.interrupt();
            httpServerThread = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static public BoosterService getService() {
        return service;
    }

    public void sendResult(int connectionId, String result) {
        webSocket.sendResult(connectionId, result);
    }

    public String[] getOpenConnections() {
        return webSocket.getOpenConnections();
    }
}
