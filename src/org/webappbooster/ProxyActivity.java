package org.webappbooster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ProxyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String action = extras.getString("ACTION");
        int id = extras.getInt("ID");
        if (action.equals("CALL_ACTIVITY")) {
            Intent intent = extras.getParcelable("INTENT");
            startActivityForResult(intent, id);
        }
        if (action.equals("CALL_PLUGIN")) {
            PluginManager.runPlugin(this, id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PluginManager.resultFromActivity(requestCode, resultCode, data);
        finish();
    }
}
