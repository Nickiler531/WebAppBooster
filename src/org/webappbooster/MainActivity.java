package org.webappbooster;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    static public Activity activity;

    private BoosterService boundService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        activity = this;
        setContentView(R.layout.activity_main);
    	ListView listView = (ListView) findViewById(R.id.list_connections);
    	listView.setOnItemClickListener(new OnItemClickListener() {
        	  @Override
        	  public void onItemClick(AdapterView<?> parent, View view,
        	    int position, long id) {
        	    Toast.makeText(getApplicationContext(),
        	      "Click ListItem Number " + position, Toast.LENGTH_LONG)
        	      .show();
        	  }
        	}); 
    }

    @Override
    public void onResume() {
    	super.onResume();
    	refreshConnections();
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

    private void refreshConnections() {
    	ListView listView = (ListView) findViewById(R.id.list_connections);
    	String[] values = new String[] {};
    	BoosterService service = BoosterService.getService();
    	if (service != null) {
    		values = service.getOpenConnections();
    	}
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
    	  android.R.layout.simple_list_item_1, android.R.id.text1, values);
    	listView.setAdapter(adapter); 
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
