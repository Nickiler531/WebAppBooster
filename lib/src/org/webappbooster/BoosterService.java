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
import org.webappbooster.lib.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BoosterService extends Service {

    final static public String    PARAM_WEBSOCKET_PORT    = "WEBSOCKET_PORT";
    final static public String    PARAM_HTTP_PORT         = "HTTP_PORT";
    final static public String    PARAM_SHOW_NOTIFICATION = "SHOW_NOTIFICATION";
    final static public String    PARAM_TOKEN             = "TOKEN";

    private IBinder               binder                  = new LocalBinder();

    private BoosterWebSocket      webSocket               = null;
    private Connection            httpSocket              = null;
    private Thread                httpServerThread        = null;

    static private BoosterService service                 = null;

    private int                   paramPortWS;
    private int                   paramPortHttp;
    private boolean               paramShowNotification;
    private double                paramToken;

    private PluginManager         pluginManager;
    private Notification          notification;
    private PendingIntent         contentIntent;

    public class LocalBinder extends Binder {
        public BoosterService getService() {
            return BoosterService.this;
        }
    }

    @Override
    public void onCreate() {
        paramShowNotification = true;
        paramToken = 0;

        service = this;
        pluginManager = new PluginManager();
        Log.d("WAB", "Starting WebAppBooster service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (webSocket != null) {
            Log.d("WAB", "Warning: WAB server already started");
            return START_STICKY;
        }
        paramPortWS = intent.getIntExtra(PARAM_WEBSOCKET_PORT, 0);
        paramPortHttp = intent.getIntExtra(PARAM_HTTP_PORT, 0);
        paramToken = intent.getDoubleExtra(PARAM_TOKEN, 0);
        paramShowNotification = intent.getBooleanExtra(PARAM_SHOW_NOTIFICATION, false);
        startWebSocketServer();
        startHttpServer();
        showNotificationIcon();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        service = null;
        Log.d("WAB", "Stopping WebAppBooster service");
        hideNotificationIcon();
        stopWebSocketServer();
        stopHttpServer();
    }

    @SuppressWarnings("deprecation")
    private void showNotificationIcon() {
        if (!paramShowNotification) {
            return;
        }
        notification = new Notification(R.drawable.ic_stat_wab, getString(R.string.app_name),
                System.currentTimeMillis());
        notification.flags |= Notification.FLAG_NO_CLEAR;
        // Create intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Create PendingIntent
        contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        updateNotification();
    }

    private void hideNotificationIcon() {
        if (!paramShowNotification) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    private void startWebSocketServer() {
        // WebSocket.DEBUG = true;
        try {
            if (webSocket == null) {
                webSocket = new BoosterWebSocket(paramPortWS);
                webSocket.start();
            }
        } catch (UnknownHostException e) {
            webSocket = null;
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
                    InetSocketAddress address = new InetSocketAddress(paramPortHttp);
                    httpSocket.connect(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        httpServerThread.start();
    }

    private void stopHttpServer() {
        if (httpSocket == null) {
            return;
        }
        try {
            httpSocket.close();
            httpServerThread.join();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        httpSocket = null;
        httpServerThread = null;
    }

    static public BoosterService getService() {
        return service;
    }

    public void sendResult(int connectionId, String result) {
        webSocket.sendResult(connectionId, result);
    }

    public int getWebSocketPort() {
        return webSocket.getPort();
    }

    public double getToken() {
        return paramToken;
    }

    public String[] getOpenConnections() {
        return webSocket.getOpenConnections();
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public void updateNotification() {
        if (!paramShowNotification) {
            return;
        }
        notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                getString(R.string.num_active_connections, getOpenConnections().length),
                contentIntent);
        // Get NotificationManager and show Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
