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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroscopePlugin extends Plugin implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        sensorGyroscope;
    private Request       request;

    @Override
    public void onCreate(String origin) {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    public void execute(Request request) throws JSONException {
        String action = request.getAction();
        if (action.equals("START_GYRO")) {
            this.request = request;
            sensorManager
                    .registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (action.equals("STOP_GYRO")) {
            sensorManager.unregisterListener(this);
            JSONObject sensorEvent = new JSONObject();
            sensorEvent.put("startId", this.request.getRequestId());
            sendResult(request.getRequestId(), sensorEvent);
        }
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            JSONObject sensorEvent = new JSONObject();
            sensorEvent.put("x", event.values[0]);
            sensorEvent.put("y", event.values[1]);
            sensorEvent.put("z", event.values[2]);
            sendResult(request.getRequestId(), sensorEvent);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
