package org.webappbooster;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    static public Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        activity = this;
        setContentView(R.layout.activity_main);
        View textView = findViewById(R.id.status_active);
        textView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showSettings();
            }
        });
        ListView listView = (ListView) findViewById(R.id.list_connections);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PermissionsDialog w = new PermissionsDialog(MainActivity.this);
                w.requestPermissions("http://pear", new String[] { "Permission 1", "Permission 2" });
                w.show(new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("WAB", "Clicked: " + which);
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            showSettings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void refreshView() {
        TextView statusView = (TextView) findViewById(R.id.status_active);
        ListView listView = (ListView) findViewById(R.id.list_connections);
        View noConnectionView = findViewById(R.id.text_no_connections);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableWAB = sharedPref.getBoolean(SettingsActivity.PREF_KEY_ENABLE_WAB, false);
        if (enableWAB) {
            startBoosterService();
            statusView.setText(R.string.booster_active);
        } else {
            stopBoosterService();
            statusView.setText(R.string.booster_deactive);
        }

        String[] values = new String[] {};
        BoosterService service = BoosterService.getService();
        if (service != null) {
            values = service.getOpenConnections();
        }

        values = new String[] { "ABC", "123" };
        if (values.length == 0) {
            listView.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.VISIBLE);
        } else {
            noConnectionView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, values);
            listView.setAdapter(adapter);
        }
    }

    private void startBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.startService(intent);
    }

    private void stopBoosterService() {
        Intent intent = new Intent(this, BoosterService.class);
        this.stopService(intent);
    }
}
