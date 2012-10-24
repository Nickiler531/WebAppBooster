package org.webappbooster;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

public class ProxyActivity extends Activity {

    final public static int PICK_CONTACT = 0;

    private int id;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        int action = extras.getInt("ACTION");
        id = extras.getInt("ID");
        switch (action) {
        case PICK_CONTACT:
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, PICK_CONTACT);
            break;
        default:
            Log.d("TouchDevelop", "Received bad action in ProxyActivity (" + action + ")");
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case (PICK_CONTACT):
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = c.getString(c
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Cursor emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null,
                            null);
                    String emailAddress = "";
                    if (emails.moveToFirst()) {
                        emailAddress = emails.getString(emails
                                .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    }
                    emails.close();
                    JSONObject result = new JSONObject();
                    try {
                        result.put("status", "ok");
                        result.put("name", name);
                        result.put("email", emailAddress);
                        returnResultToService(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                c.close();
            }
            break;
        }
        finish();
    }

    private void returnResultToService(final JSONObject result) {
            Intent intent = new Intent(this, BoosterService.class);
            this.bindService(intent, new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ((BoosterService.LocalBinder) service).getService().resultFromProxy(id, result.toString());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, 0);
    }
}
