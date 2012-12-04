package org.webappbooster.plugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.WebSocketInfo;

import android.content.Intent;
import android.net.Uri;

public class AuthenticationPlugin extends Plugin {

    private Intent intent;

    @Override
    public void execute(int requestId, String action, JSONObject request) throws JSONException {
        WebSocketInfo info = this.getConnectionInfo();
        if (action.equals("REQUEST_AUTHENTICATION")) {
            String path = request.getString("path");
            path += "#webappbooster_token=" + info.getToken();
            String url = info.getOrigin() + "/" + path;
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            info.closeConnection();
            runInContextOfProxyActivity();
        } else {
            double token = request.getDouble("token");
            int status = 0;
            if (token == info.getToken()) {
                info.connectionIsAuthorized();
            } else {
                status = -1;
            }
            JSONObject result = new JSONObject();
            result.put("status", status);
            sendResult(requestId, result);
        }
    }

    @Override
    public void callbackFromProxy() throws JSONException {
        getContext().startActivity(intent);
        finishProxyActivity();
    }
}
