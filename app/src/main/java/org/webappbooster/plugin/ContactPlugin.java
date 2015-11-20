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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.webappbooster.HTTPServer;
import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

@PluginMappingAnnotation(actions = "PICK_CONTACT|LIST_CONTACTS", permission = "READ_CONTACTS")
public class ContactPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("PICK_CONTACT")) {
            executePickContact(request);
        } else if (action.equals("LIST_CONTACTS")) {
            executeListContacts(request);
        }
    }

    private void executePickContact(Request request) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        callActivity(request, intent);
    }

    private void executeListContacts(Request request) {
        String query = request.getString("query");
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String selection = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " = ?";
        String[] selectionArguments = { query };
        Cursor cursor = contentResolver.query(uri, null, selection, selectionArguments, null);
        Response response;
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Data.CONTACT_ID));
            response = request.createResponse(Response.OK);
            populateResponse(response, contactId);
            response.send();
        }
        cursor.close();
        response = request.createResponse(Response.OK);
        response.lastForId(request.getRequestId());
        response.send();
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Response response = request.createResponse(Response.ERR_CANCELLED);
            response.send();
            return;
        }
        Uri contactData = data.getData();
        String contactId = contactData.getLastPathSegment();
        Response response = request.createResponse(Response.OK);
        populateResponse(response, contactId);
        response.send();
    }

    private void populateResponse(Response response, String contactId) {
        ContentResolver resolver = getContext().getContentResolver();

        // Get name
        String nameGiven = null;
        String nameMiddle = null;
        String nameFamily = null;
        String nameDisplay = null;

        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND "
                + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
        String[] whereNameParams = new String[] {
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId };
        Cursor nameCur = resolver.query(ContactsContract.Data.CONTENT_URI, null, whereName,
                whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        if (nameCur.moveToNext()) {
            nameGiven = nameCur.getString(nameCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            nameMiddle = nameCur.getString(nameCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
            nameFamily = nameCur.getString(nameCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            nameDisplay = nameCur.getString(nameCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
        }
        nameCur.close();

        // Get various phone numbers
        String phoneMain = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_MAIN);
        String phoneHome = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        String phoneWork = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        String phoneMobile = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        String phoneOther = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        String faxHome = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME);
        String faxWork = getPhone(resolver, contactId,
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);

        // Get various email addresses
        String emailHome = getEmail(resolver, contactId,
                ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        String emailWork = getEmail(resolver, contactId,
                ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        String emailOther = getEmail(resolver, contactId,
                ContactsContract.CommonDataKinds.Email.TYPE_OTHER);

        // Get various addresses
        String addressHome = getAddress(resolver, contactId,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);
        String addressWork = getAddress(resolver, contactId,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        String addressOther = getAddress(resolver, contactId,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER);

        Uri photoUri = getPhotoUri(resolver, contactId);

        // Send contact
        response.add("nameGiven", nameGiven);
        response.add("nameMiddle", nameMiddle);
        response.add("nameFamily", nameFamily);
        response.add("nameDisplay", nameDisplay);
        response.add("phoneMain", phoneMain);
        response.add("phoneHome", phoneHome);
        response.add("phoneWork", phoneWork);
        response.add("phoneMobile", phoneMobile);
        response.add("phoneOther", phoneOther);
        response.add("faxHome", faxHome);
        response.add("faxWork", faxWork);
        response.add("emailHome", emailHome);
        response.add("emailWork", emailWork);
        response.add("emailOther", emailOther);
        response.add("addressHome", addressHome);
        response.add("addressWork", addressWork);
        response.add("addressOther", addressOther);
        if (photoUri != null) {
            Log.d("WAB", photoUri.toString());
            response.add(
                    "photoUri",
                    HTTPServer.genResourceUri(this.getConnectionInfo().getToken(),
                            photoUri.toString(), "image/png"));
        }

    }

    private String getPhone(ContentResolver resolver, String contactId, int type) {
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + type,

                new String[] { contactId }, null);

        String phone = null;
        if (cursor.moveToFirst()) {
            phone = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursor.close();
        return phone;
    }

    private String getEmail(ContentResolver resolver, String contactId, int type) {
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS },

                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND "
                        + ContactsContract.CommonDataKinds.Email.TYPE + " = " + type,

                new String[] { contactId }, null);

        String email = null;
        if (cursor.moveToFirst()) {
            email = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
        }
        cursor.close();
        return email;
    }

    private String getAddress(ContentResolver resolver, String contactId, int type) {
        Cursor cursor = resolver
                .query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS },

                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND "
                                + ContactsContract.CommonDataKinds.StructuredPostal.TYPE + " = "
                                + type,

                        new String[] { contactId }, null);

        String address = null;
        if (cursor.moveToFirst()) {
            address = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
        }
        cursor.close();
        return address;
    }

    public Uri getPhotoUri(ContentResolver resolver, String contactId) {
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                Long.parseLong(contactId));
        Uri photoUri = Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        try {
            InputStream is = resolver.openInputStream(photoUri);
            is.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        return photoUri;
    }
}
