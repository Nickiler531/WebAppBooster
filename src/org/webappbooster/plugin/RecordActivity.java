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

import java.io.File;

import org.webappbooster.util.ExtAudioRecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class RecordActivity extends Activity {

    final private static String PATH          = "/WebAppBooster/";

    private static String       mFileName     = null;

    private RecordButton        mRecordButton = null;

    private ExtAudioRecorder    extAudioRecorder;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        extAudioRecorder = ExtAudioRecorder.getInstanse(true);
        extAudioRecorder.setOutputFile(mFileName);
        extAudioRecorder.prepare();
        extAudioRecorder.start();
    }

    private void stopRecording() {
        extAudioRecorder.stop();
        extAudioRecorder.release();
        extAudioRecorder = null;
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    onRecord(mStartRecording);
                    if (mStartRecording) {
                        setText("Stop recording");
                        mStartRecording = false;
                    } else {
                        Intent intent = getIntent();
                        intent.putExtra("path", mFileName);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        }
    }

    public RecordActivity() {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mFileName = dir.getAbsolutePath() + "/recording.wav";
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        RelativeLayout layout = new RelativeLayout(this);
        mRecordButton = new RecordButton(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(mRecordButton, params);
        setContentView(layout);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (extAudioRecorder != null) {
            extAudioRecorder.release();
            extAudioRecorder = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
    }
}