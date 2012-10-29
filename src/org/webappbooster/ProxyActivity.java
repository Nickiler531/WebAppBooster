package org.webappbooster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
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

    private int             id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        JSONObject request;
        try {
            request = new JSONObject(extras.getString("REQUEST"));
            String action = request.getString("action");
            String origin = extras.getString("ORIGIN");
            id = extras.getInt("ID");
            if (action.equals("REQUEST_PERMISSION")) {
                doRequestPermission(origin, request, id);
            } else if (action.equals("PICK_CONTACT")) {
                doPickContact(origin, request, id);
            } else {
                Log.d("TouchDevelop", "Received bad action in ProxyActivity (" + action + ")");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void doRequestPermission(final String origin, JSONObject request, final int id)
            throws JSONException {
        JSONArray permissions = request.getJSONArray("permissions");
        final String[] p = new String[permissions.length()];
        for (int i = 0; i < p.length; i++) {
            p[i] = permissions.getString(i);
        }
        PermissionsDialog w = new PermissionsDialog(this);
        w.requestPermissions(origin, p);
        w.show(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != DialogInterface.BUTTON_NEGATIVE) {
                    Authorization
                            .setPermissions(origin, p, which == DialogInterface.BUTTON_NEUTRAL);
                }
                try {
                    JSONObject result = new JSONObject();
                    result.put("permission_granted", which != DialogInterface.BUTTON_NEGATIVE);
                    returnResultToService(id, result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d("WAB", "Clicked: " + which);
            }
        });

    }

    private void doPickContact(String origin, JSONObject request, int id) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case (PICK_CONTACT):
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String _id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = c.getString(c
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    Cursor emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + _id, null,
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
                        returnResultToService(id, result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                c.close();
            }
            break;
        }
    }

    private void returnResultToService(final int id, final JSONObject result) {
        Intent intent = new Intent(this, BoosterService.class);
        this.bindService(intent, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((BoosterService.LocalBinder) service).getService().resultFromProxy(id,
                        result.toString());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, 0);
        finish();
    }
}
