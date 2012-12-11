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
import org.webappbooster.MainActivity;
import org.webappbooster.Plugin;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * The Gallery plugin provides access to image files stored in internal and
 * external (i.e., /sdcard/) memory. If also provides its own caching mechanism
 * for thumbnails to allow for arbitrary thumbnail resolutions.
 */
public class GalleryPlugin extends Plugin {

    final private static String PATH = "/WebAppBooster/";

    /**
     * Helper class to tell the media subsystem to scan a new file. This class
     * is used to tell the Gallery app that there is a new iamge.
     */
    class MediaScanner implements MediaScannerConnectionClient {

        private MediaScannerConnection ms;
        private File                   file;

        public MediaScanner(Context context, File f) {
            file = f;
            ms = new MediaScannerConnection(context, this);
            ms.connect();
        }

        @Override
        public void onMediaScannerConnected() {
            ms.scanFile(file.getAbsolutePath(), null);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.d("XXX", uri == null ? "Scan failed" : path);
            ms.disconnect();
        }

    }

    @Override
    public void execute(int requestId, String action, JSONObject request) throws JSONException {
        if (action.equals("CREATE_IMAGE_THUMBNAIL")) {
            executeCreateImageThumbnail(requestId, request);
        } else if (action.equals("LIST_IMAGES")) {
            executeListImages(requestId);
        } else if (action.equals("SAVE_TO_GALLERY")) {
            executeSaveToGallery(requestId, request);
        }
    }

    /**
     * Save an image back to the Gallery. The image needs to be passed as a data
     * URL. The image will be stored in directory
     * /TouchDevelop/TouchDevelopGallery. Writing of the image happens in a
     * thread. Class MediaScanner is used to inform the Gallery app of the new
     * image.
     */
    private void executeSaveToGallery(final int requestId, final JSONObject request)
            throws JSONException {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + PATH + "Gallery");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File imageFile;
        try {
            imageFile = File.createTempFile("img", ".png", dir);
        } catch (IOException e1) {
            // TODO
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String dataURL = request.getString("url");
                    byte[] data = dataURL.substring("data:image/png;base64,".length()).getBytes();
                    byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                    FileOutputStream out = new FileOutputStream(imageFile);
                    out.write(bytes);
                    out.close();
                    new MediaScanner(MainActivity.activity, imageFile);
                    JSONObject result = new JSONObject();
                    result.put("id", requestId);
                    result.put("status", 0);
                    result.put("imageFile", imageFile.getName());
                    sendResult(requestId, result);
                } catch (FileNotFoundException e) {
                    // Do nothing
                } catch (IOException e) {
                    // Do nothing
                } catch (JSONException e) {
                    // Do nothing
                }
            }

        }).start();
    }

    /**
     * Creates a thumbnail for a given image and a given resolution. If the
     * cache does not contain an appropriate thumbnail image, it is computed and
     * stored in the cache.
     */
    private void executeCreateImageThumbnail(final int requestId, JSONObject request)
            throws JSONException {
        final String uri = request.getString("uri");
        final int width = request.getInt("width");
        final int height = request.getInt("height");
        new Thread(new Runnable() {

            @Override
            public void run() {

                JSONObject result = new JSONObject();
                int status = 0;
                URI thumbUri = null;
                if (uri != null && uri.length() > 0) {
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdcard.getAbsolutePath() + PATH + "thumbnail-cache");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        // Do not use the ".jpg" extension, otherwise Android
                        // will add
                        // the thumbnail to the gallery!
                        String fn = "" + width + "x" + height + "-"
                                + Uri.parse(uri).getLastPathSegment() + "-jpg";
                        File file = new File(dir, fn);
                        thumbUri = file.toURI();
                        if (!file.exists()) {
                            generateThumbnail(uri, width, height, new FileOutputStream(file));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    status = -1;
                }
                try {
                    result.put("id", requestId);
                    result.put("status", status);
                    if (thumbUri != null) {
                        result.put("thumbUri", thumbUri.toString());
                    }
                    sendResult(requestId, result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }).start();
    }

    /**
     * A helper method to generate a thumbnail. This method is synchronized to
     * guarantee that one thumbnail is generated after another (and not in
     * parallel). This is necessary since generating multiple thumbnails in
     * parallel causes an out of memory exception (even generating two
     * thumbnails in parallel already exhausts the memory on a Galaxy Nexus).
     * The result is a URI to the thumbnail that can be used for the HTML
     * img.src attribute.
     */
    private synchronized void generateThumbnail(String uri, int width, int height,
            FileOutputStream out) throws FileNotFoundException, IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                MainActivity.activity.getContentResolver(), Uri.parse(uri));
        Bitmap bmThumbnail = ThumbnailUtils.extractThumbnail(bitmap, width, height);

        bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 55, out);
    }

    /**
     * Retrieve all images stored in internal and external memory. The result is
     */
    private void executeListImages(final int requestId) throws JSONException {
        new Thread(new Runnable() {

            @Override
            public void run() {
                JSONArray gallery = new JSONArray();
                JSONObject result = new JSONObject();
                try {
                    scanForImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, gallery);
                    scanForImages(MediaStore.Images.Media.INTERNAL_CONTENT_URI, gallery);
                    result.put("id", requestId);
                    result.put("status", 0);
                    result.put("gallery", gallery);
                } catch (JSONException e) {
                    // TODO
                }
                sendResult(requestId, result);
            }

        }).start();
    }

    /**
     * Helper method for retrieving images in a given memory area (either
     * internal or external). The result is an array of object where each object
     * describes the properties of one image. There is currently only one
     * property called <code>uri</code> that contains the URI to the image.
     */
    private void scanForImages(Uri uri, JSONArray result) throws JSONException {
        String[] projection = { MediaStore.Images.Media._ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME };
        Cursor imageCursor = MainActivity.activity.managedQuery(uri, projection, null, null, null);
        for (imageCursor.moveToFirst(); !imageCursor.isAfterLast(); imageCursor.moveToNext()) {
            int id = imageCursor.getInt(imageCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
            String bucketName = imageCursor.getString(imageCursor
                    .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
            JSONObject image = new JSONObject();
            image.put("uri", Uri.withAppendedPath(uri, "" + id));
            image.put("galleryName", bucketName);
            result.put(image);
        }
    }
}