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

package org.webappbooster;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.webappbooster.util.ACRAPostSender;

import java.util.HashMap;

@ReportsCrashes(formUri = "", mode = ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.acra_toast_text)
public class BoosterApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ACRA.init(this);
        HashMap<String, String> ACRAData = new HashMap<String, String>();
        ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));
    }

    public static Context getAppContext() {
        return context;
    }
}