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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    final static public String VERSION = "1.0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        View textView = findViewById(R.id.status_view);
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
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                final String origin = adapter.getItem(position);
                String[] permissions = Authorization.getPermissions(origin);
                w.showPermissions(origin, permissions);
                w.show(new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            Authorization.revokePermissions(origin);
                        }
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
        ImageView imageView = (ImageView) findViewById(R.id.icon_active);
        ListView listView = (ListView) findViewById(R.id.list_connections);
        View connectionView = findViewById(R.id.open_connections);
        View noConnectionView = findViewById(R.id.text_no_connections);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableWAB = sharedPref.getBoolean(SettingsActivity.PREF_KEY_ENABLE_WAB, false);
        if (enableWAB) {
            startBoosterService();
            statusView.setText(R.string.booster_active);
            imageView.setImageResource(R.drawable.enabled);
        } else {
            stopBoosterService();
            statusView.setText(R.string.booster_deactive);
            imageView.setImageResource(R.drawable.disabled);
        }

        String[] values = new String[] {};
        BoosterService service = BoosterService.getService();
        if (service != null) {
            values = service.getOpenConnections();
        }

        if (values.length == 0) {
            connectionView.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.VISIBLE);
        } else {
            noConnectionView.setVisibility(View.GONE);
            connectionView.setVisibility(View.VISIBLE);
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
