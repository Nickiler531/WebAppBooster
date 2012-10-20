package org.webappbooster;

import java.util.Random;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements ServiceConnection {

    static public Activity activity;

    private Intent         serviceIntent;

    private long           token;

    private BoosterService boundService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        token = new Random().nextLong();
        serviceIntent = new Intent(this, BoosterService.class);
        this.startService(serviceIntent);
        this.bindService(serviceIntent, this, 0);

        setContentView(R.layout.activity_main);

        Button button = (Button) this.findViewById(R.id.button_booster);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView t = (TextView) MainActivity.this.findViewById(R.id.status_active);
                Button b = (Button) v;
                if (boundService.isWebSocketOpen()) {
                    boundService.closeWebSocket();
                    b.setText(R.string.start_booster);
                    t.setText(R.string.booster_deactive);
                } else {
                    boundService.openWebSocket();
                    b.setText(R.string.stop_booster);
                    t.setText(R.string.booster_active);
                }
            }

        });
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        boundService = ((BoosterService.LocalBinder) service).getService();
        // Uri uri = Uri.parse("http://localhost:8080/www/index.html?token=" +
        // token);
        // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // this.startActivity(intent);
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        boundService = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
