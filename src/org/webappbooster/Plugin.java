package org.webappbooster;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public abstract class Plugin {

    private Context context;
    private int     connectionId;

    abstract public void execute(JSONObject request);

    public void setContext(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    public void setConnectionId(int id) {
        this.connectionId = id;
    }

    public void onCreate(String origin) {
        // Do nothing
    }

    public void onDestroy() {
        // Do nothing
    }

    protected void callActivity(Intent intent) {
        PluginManager.callActivityViaProxy(this, intent);
    }

    public void resultFromActivity(int resultCode, Intent data) {
        // Do nothing
    }

    protected void sendResultAndExit(JSONObject result) {
        sendResult(result);
        // final String r = result.toString();
        // final Intent intent = new Intent(context, BoosterService.class);
        // context.bindService(intent, new ServiceConnection() {
        //
        // @Override
        // public void onServiceConnected(ComponentName name, IBinder service) {
        // ((BoosterService.LocalBinder) service).getService()
        // .resultFromProxy(connectionId, r);
        // context.unbindService(this);
        // }
        //
        // @Override
        // public void onServiceDisconnected(ComponentName name) {
        // }
        // }, 0);

        if (context instanceof ProxyActivity) {
            ((ProxyActivity) context).finish();
        }
    }

    protected void sendResult(JSONObject result) {
        BoosterService.getService().resultFromProxy(connectionId, result.toString());
    }

    protected void runInContextOfProxyActivity() {
        PluginManager.runViaProxy(this);
    }

    public void callbackFromProxy() {
        // Do nothing
    }
}
