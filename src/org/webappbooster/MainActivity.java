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

public class MainActivity extends Activity {

    static public Activity activity;

    private long           token;

    private BoosterService boundService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        token = new Random().nextLong();

        setContentView(R.layout.activity_main);

        Button button = (Button) this.findViewById(R.id.button_booster);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView t = (TextView) MainActivity.this.findViewById(R.id.status_active);
                Button b = (Button) v;
                if (BoosterService.isServiceRunning()) {
                    stopBoosterService();
                    b.setText(R.string.start_booster);
                    t.setText(R.string.booster_deactive);
                } else {
                    startBoosterService();
                    b.setText(R.string.stop_booster);
                    t.setText(R.string.booster_active);
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private void startBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.startService(intent);
        this.bindService(intent, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                boundService = ((BoosterService.LocalBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                boundService = null;
            }
        }, 0);

    }

    private void stopBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.stopService(intent);
    }
}
