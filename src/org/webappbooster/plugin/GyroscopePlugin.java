package org.webappbooster.plugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroscopePlugin extends Plugin implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        sensorGyroscope;

    @Override
    public void onCreate(String origin) {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    public void execute(String action, JSONObject request) {
        if (action.equals("START_GYRO")) {
            sensorManager
                    .registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (action.equals("STOP_GYRO")) {
            sensorManager.unregisterListener(this);
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
            sendResult(sensorEvent);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
