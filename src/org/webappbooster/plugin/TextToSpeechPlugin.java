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
import java.util.Locale;

import org.webappbooster.Plugin;
import org.webappbooster.PluginMappingAnnotation;
import org.webappbooster.R;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
@PluginMappingAnnotation(actions="SPEAK_TEXT",permission="")
public class TextToSpeechPlugin extends Plugin implements OnInitListener,
        OnUtteranceCompletedListener, OnClickListener {

    private TextToSpeech tts = null;
    private Request      request;

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("SPEAK_TEXT")) {
            executeSpeakText(request);
        }
    }

    private void executeSpeakText(Request request) {
        this.request = request;
        if (tts == null) {
            Intent intent = new Intent();
            intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            callActivity(request, intent);
        } else {
            doSpeak();
        }
    }

    @Override
    public void resultFromActivity(Request request, int resultCode, Intent data) {
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // success, create the TTS instance
            tts = new TextToSpeech(getContext(), this);
        } else {
            runInContextOfProxyActivity(request);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.ERROR) {
            sendError();
        } else {
            tts.setOnUtteranceCompletedListener(this);
            doSpeak();
        }
    }

    @Override
    public void callbackFromProxy(final Request request) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_install_tts);
        builder.setMessage(R.string.install_tts);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendError() {
        Response response = request.createResponse(Response.ERR_CANCELLED);
        response.send();
    }

    private void doSpeak() {
        String language = request.getString("language");
        Locale locale = Locale.getDefault();
        if (language.equals("en")) {
            locale = Locale.ENGLISH;
        }
        if (language.equals("fr")) {
            locale = Locale.FRENCH;
        }
        if (language.equals("de")) {
            locale = Locale.GERMAN;
        }
        if (language.equals("it")) {
            locale = Locale.ITALIAN;
        }
        tts.setLanguage(locale);
        String text = request.getString("text");
        // Some dummy params are needed. Otherwise the
        // OnUtteranceCompletedListener won't be called.
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "theUtId");
        tts.speak(text, TextToSpeech.QUEUE_ADD, params);
    }

    @Override
    public void onUtteranceCompleted(String arg0) {
        Response response = request.createResponse(Response.OK);
        response.send();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // missing data, install it
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(installIntent);
        }
        // Since the TTS module is currently not installed, we always send an
        // error
        sendError();
    }
}
