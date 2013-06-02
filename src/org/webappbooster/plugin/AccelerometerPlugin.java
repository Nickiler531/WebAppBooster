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

public class AccelerometerPlugin extends Plugin implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        sensorAccelerometer;
    private Request       startRequest;

    @Override
    public void onCreate(String origin) {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    public void execute(Request request) {
        String action = request.getAction();
        if (action.equals("START_ACCELEROMETER")) {
            startRequest = request;
            sensorManager.registerListener(this, sensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else if (action.equals("STOP_ACCELEROMETER")) {
            sensorManager.unregisterListener(this);
            Response response = request.createResponse(Response.OK);
            response.lastForId(startRequest.getRequestId());
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
