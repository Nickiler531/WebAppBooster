package org.webappbooster;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BoosterService extends Service {

    final private static int PORT           = 8080;

    private IBinder          binder         = new LocalBinder();
    private BoosterWebSocket webSocket      = null;

    static private BoosterService   service = null;

    class LocalBinder extends Binder {
        BoosterService getService() {
            return BoosterService.this;
        }
    }

    @Override
    public void onCreate() {
        service = this;
        openWebSocket();

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
        service = null;;
        closeWebSocket();
    }

    private void openWebSocket() {
        // WebSocket.DEBUG = true;
        try {
            if (webSocket == null) {
                webSocket = new BoosterWebSocket(this, PORT);
                webSocket.start();
            }
        } catch (UnknownHostException e) {
            webSocket = null;
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void closeWebSocket() {
        if (webSocket != null) {
            try {
                webSocket.stop();
            } catch (IOException e) {
            }
            webSocket = null;
        }
    }

    static public BoosterService getService() {
        return service;
    }

    public void resultFromProxy(int id, String result) {
        webSocket.resultFromProxy(id, result);
    }
    
    public String[] getOpenConnections() {
    	return webSocket.getOpenConnections();
    }
}
