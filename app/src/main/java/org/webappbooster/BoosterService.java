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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class BoosterService extends Service {

    private IBinder               binder           = new LocalBinder();
    private BoosterWebSocket      webSocket        = null;
    private Connection            connection       = null;

    static private BoosterService service          = null;

    private PluginManager         pluginManager;
    private Notification          notification;
    private PendingIntent         contentIntent;

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
        showNotificationIcon();
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
        hideNotificationIcon();
        stopWebSocketServer();
        stopHttpServer();
    }

    @SuppressWarnings("deprecation")
    private void showNotificationIcon() {
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
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    private void startWebSocketServer() {
        // WebSocket.DEBUG = true;
        try {
            if (webSocket == null) {
                webSocket = new BoosterWebSocket(this);
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

    public void startHttpServer() {
        if (connection != null) {
            return;
        }
        try {
            Container container = new HTTPServer();
            ContainerSocketProcessor processor = new ContainerSocketProcessor(container, 3);
            connection = new SocketConnection(processor);
            SocketAddress address = new InetSocketAddress(Config.PORT_HTTP);
            connection.connect(address);

        } catch (IOException ex) {
            Log.e("WAB", "Could not start HTTP server: " + ex.getMessage());
        }

    }

    public void stopHttpServer() {
        try {
            connection.close();
        } catch (IOException ex) {
            Log.e("WAB", "Could not stop HTTP server: " + ex.getMessage());
        }
        connection = null;
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

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public void updateNotification() {
        notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name),
                getString(R.string.num_active_connections, getOpenConnections().length),
                contentIntent);
        // Get NotificationManager and show Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
