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

import org.webappbooster.Plugin;
import org.webappbooster.Request;
import org.webappbooster.Response;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroscopePlugin extends Plugin implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        sensorGyroscope;
    private Request       startRequest;

    @Override
    public void onCreate(String origin) {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("START_GYRO")) {
            this.startRequest = request;
            sensorManager
                    .registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (action.equals("STOP_GYRO")) {
            sensorManager.unregisterListener(this);
            Response response = request.createResponse(Response.OK);
            response.add("removeCallbackId", startRequest.getRequestId());
            response.send();
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
        Response response = startRequest.createResponse(Response.OK);
        response.add("x", event.values[0]);
        response.add("y", event.values[1]);
        response.add("z", event.values[2]);
        response.send();
    }

}
