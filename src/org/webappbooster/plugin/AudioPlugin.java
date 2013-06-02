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

import java.util.HashMap;

import org.webappbooster.BoosterApplication;
import org.webappbooster.Plugin;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * The Audio plugin provides access to songs stored on the device.
 */
public class AudioPlugin extends Plugin {

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("LIST_SONGS")) {
            executeListSongs(request);
        }
    }

    /**
     * Retrieve all songs stored on the device.
     */
    synchronized private void executeListSongs(final Request request) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                scanForSongs(request, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                Response response = request.createResponse(Response.OK);
                response.lastForId(request.getRequestId());
                response.send();
            }

        }).start();
    }

    // synchronized private void executeGetGenreForSong(int requestId,
    // JSONObject request) {
    // JSONObject result;
    // String data;
    // try {
    // data = request.getString("url");
    // String genre = getGenre(data);
    // result = new PluginResult(PluginResult.Status.OK, new
    // JSONArray().put(genre));
    // } catch (JSONException e) {
    // result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    // }
    // return result;
    // }

    /**
     * Helper method for retrieving songs stored on the device. The result is an
     * array of objects where each object describes the properties of one song.
     */
    private void scanForSongs(Request request, Uri uri) {
        String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK };
        Cursor cursor = BoosterApplication.getAppContext().getContentResolver()
                .query(uri, projection, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            ;
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String duration = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
            Response response = request.createResponse(Response.OK);
            response.add(
                    "uri",
                    sendResourceViaHTTP(Uri.withAppendedPath(uri, "" + id).toString(), "audio/mpeg"));
            response.add("data", data);
            response.add("title", title);
            response.add("artist", artist);
            response.add("album", album);
            if (duration != null) {
                response.add("duration", Integer.parseInt(duration));
            }
            if (track != null) {
                response.add("track", Integer.parseInt(track));
            }
            response.send();
        }
        cursor.close();
    }

    private HashMap<String, String> genreIdMap = null;

    private String getGenre(String path) {
        Cursor cursor = null;
        if (genreIdMap == null) {
            genreIdMap = new HashMap<String, String>();
            cursor = BoosterApplication
                    .getAppContext()
                    .getContentResolver()
                    .query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                            new String[] { MediaStore.Audio.Genres._ID,
                                    MediaStore.Audio.Genres.NAME }, null, null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                genreIdMap.put(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres.NAME)));
            }
            cursor.close();
            cursor = null;
        }
        String genre = "";
        for (String genreId : genreIdMap.keySet()) {
            cursor = BoosterApplication
                    .getAppContext()
                    .getContentResolver()
                    .query(makeGenreUri(genreId), new String[] { MediaStore.Audio.Media.DATA },
                            MediaStore.Audio.Media.DATA + " LIKE \"" + path + "\"", null, null);
            if (cursor.getCount() != 0) {
                genre = genreIdMap.get(genreId);
                break;
            }
            cursor.close();
            cursor = null;
        }
        return genre;
    }

    private Uri makeGenreUri(String genreId) {
        return Uri.parse(new StringBuilder()
                .append(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString()).append("/")
                .append(genreId).append("/")
                .append(MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY).toString());
    }

}