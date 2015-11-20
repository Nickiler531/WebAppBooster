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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;

@PluginMappingAnnotation(actions = "LIST_APPOINTMENTS", permission = "READ_CALENDAR")
public class CalendarPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("LIST_APPOINTMENTS")) {
            executeListAppointments(request);
        }
    }

    private void executeListAppointments(Request request) {
        long startTime = request.getLong("start");
        long endTime = request.getLong("end");
        if (startTime == 0 || endTime == 0) {
            Response resp = request.createResponse(Response.ERR_MALFORMED_REQUEST);
            resp.send();
            return;
        }
        getCalendarEntries(request, startTime, endTime);
        Response response = request.createResponse(Response.OK);
        response.lastForId(request.getRequestId());
        response.send();
    }

    private static final String[] INSTANCE_PROJECTION    = new String[] { Instances.EVENT_ID, /* 0 */
                                                         Instances.BEGIN, /* 1 */
                                                         Instances.END /* 2 */
                                                         };

    // The indices for the projection array above.
    private static final int      PROJECTION_INSTANCE_ID = 0;
    private static final int      PROJECTION_BEGIN       = 1;
    private static final int      PROJECTION_END         = 2;

    private void getCalendarEntries(Request request, long startMillis, long endMillis) {
        ContentResolver cr = getContext().getContentResolver();

        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        Cursor cur = cr.query(builder.build(), INSTANCE_PROJECTION, null, null, null);

        while (cur.moveToNext()) {
            Response response = request.createResponse(Response.OK);
            // Get the field values
            long eventID = cur.getLong(PROJECTION_INSTANCE_ID);
            response.add("start", cur.getLong(PROJECTION_BEGIN));
            response.add("end", cur.getLong(PROJECTION_END));
            lookupEvent(response, eventID);
            lookupAttendees(response, eventID);
            response.send();
        }
        cur.close();
    }

    private static final String[] EVENT_PROJECTION          = new String[] { Events.CALENDAR_ID, /* 0 */
                                                            Events.ORGANIZER, /* 1 */
                                                            Events.TITLE, /* 2 */
                                                            Events.EVENT_LOCATION, /* 3 */
                                                            Events.DESCRIPTION, /* 4 */
                                                            Events.ALL_DAY, /* 5 */
                                                            };
    // The indices for the projection array above.
    private static final int      PROJECTION_CALENDAR_ID    = 0;
    private static final int      PROJECTION_ORGANIZER      = 1;
    private static final int      PROJECTION_TITLE          = 2;
    private static final int      PROJECTION_EVENT_LOCATION = 3;
    private static final int      PROJECTION_DESCRIPTION    = 4;
    private static final int      PROJECTION_ALL_DAY        = 5;

    private void lookupEvent(Response response, long eventId) {
        ContentResolver cr = getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, eventId);
        Cursor cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        if (cur.moveToNext()) {
            // Get the field values
            long calId = cur.getLong(PROJECTION_CALENDAR_ID);
            response.add("source", lookupCalendarName(calId));
            String organizerMail = cur.getString(PROJECTION_ORGANIZER);
            response.add("organizer", createContact("", organizerMail));
            response.add("subject", cur.getString(PROJECTION_TITLE));
            response.add("location", cur.getString(PROJECTION_EVENT_LOCATION));
            response.add("details", cur.getString(PROJECTION_DESCRIPTION));
            response.add("isAllDay", cur.getInt(PROJECTION_ALL_DAY) == 1);
        }
        cur.close();
    }

    private static final String[] CALENDAR_PROJECTION      = new String[] { Calendars.NAME /* 0 */};
    private static final int      PROJECTION_CALENDAR_NAME = 0;

    private String lookupCalendarName(long calendarId) {
        ContentResolver cr = getContext().getContentResolver();
        Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId);
        Cursor cur = cr.query(uri, CALENDAR_PROJECTION, null, null, null);

        String name = "";

        if (cur.moveToNext()) {
            // Get the field values
            name = cur.getString(PROJECTION_CALENDAR_NAME);
        }
        cur.close();
        return name;
    }

    private static final String[] ATTENDEE_PROJECTION              = new String[] {
            Attendees.ATTENDEE_NAME, /* 0 */
            Attendees.ATTENDEE_EMAIL, /* 1 */
            Attendees.ATTENDEE_RELATIONSHIP                       /* 2 */};

    private static final int      PROJECTION_ATTENDEE_NAME         = 0;
    private static final int      PROJECTION_ATTENDEE_EMAIL        = 1;
    private static final int      PROJECTION_ATTENDEE_RELATIONSHIP = 2;

    private void lookupAttendees(Response response, long eventId) {
        ContentResolver cr = getContext().getContentResolver();
        String selection = "(" + Attendees.EVENT_ID + " = ?)";
        String[] selectionArgs = new String[] { Long.toString(eventId) };
        Uri uri = Attendees.CONTENT_URI;
        Cursor cur = cr.query(uri, ATTENDEE_PROJECTION, selection, selectionArgs, null);

        JSONArray attendees = new JSONArray();

        while (cur.moveToNext()) {
            // Get the field values
            String name = cur.getString(PROJECTION_ATTENDEE_NAME);
            String email = cur.getString(PROJECTION_ATTENDEE_EMAIL);
            JSONObject attendee = createContact(name, email);
            int rel = cur.getInt(PROJECTION_ATTENDEE_RELATIONSHIP);
            if (rel == Attendees.RELATIONSHIP_ORGANIZER) {
                response.add("organizer", attendee);
            } else {
                attendees.put(attendee);
            }
        }
        if (attendees.length() != 0) {
            response.add("attendees", attendees);
        }
        cur.close();
    }

    private JSONObject createContact(String name, String email) {
        JSONObject contact = new JSONObject();
        try {
            contact.put("nameDisplay", name);
            contact.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contact;
    }
}
