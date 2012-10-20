package org.webappbooster;

import java.io.IOException;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BoosterService extends Service {

    final private static int PORT      = 8080;

    private IBinder          binder    = new LocalBinder();
    private BoosterWebSocket webSocket = null;

    class LocalBinder extends Binder {
        BoosterService getService() {
            return BoosterService.this;
        }
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
        if (webSocket != null) {
            try {
                webSocket.stop();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        webSocket = null;
    }

    public void openWebSocket() {
        // WebSocket.DEBUG = true;
        try {
            if (webSocket == null) {
                webSocket = new BoosterWebSocket(PORT);
                webSocket.start();
            }
        } catch (UnknownHostException e) {
            webSocket = null;
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            try {
                webSocket.stop();
            } catch (IOException e) {
            }
            webSocket = null;
        }
    }

    public boolean isWebSocketOpen() {
        return webSocket != null;
    }
}
