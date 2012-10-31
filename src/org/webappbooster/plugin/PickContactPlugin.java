package org.webappbooster.plugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class PickContactPlugin extends Plugin {

    public void execute(JSONObject request) {
        Log.d("WAB", "PickContactPlugin: " + request);
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        callActivity(intent);
    }

    @Override
    public void resultFromActivity(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        Uri contactData = data.getData();
        ContentResolver resolver = getContext().getContentResolver();
        Cursor c = resolver.query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String _id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Cursor emails = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + _id, null,
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
                sendResultAndExit(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        c.close();
    }
}
