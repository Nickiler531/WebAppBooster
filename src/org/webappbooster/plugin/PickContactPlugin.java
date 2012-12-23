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

package org.webappbooster.plugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.Request;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class PickContactPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        callActivity(request, intent);
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
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
                result.put("status", 0);
                result.put("name", name);
                result.put("email", emailAddress);
                sendResult(request.getRequestId(), result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        c.close();
    }
}
