package org.webappbooster.plugin;

import org.json.JSONException;
import org.json.JSONObject;
import org.webappbooster.Plugin;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerPlugin extends Plugin implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        sensorAccelerometer;
    private int           requestId;

    @Override
    public void onCreate(String origin) {
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    public void execute(int requestId, String action, JSONObject request) throws JSONException {
        if (action.equals("START_ACCELEROMETER")) {
            this.requestId = requestId;
            sensorManager.registerListener(this, sensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else if (action.equals("STOP_ACCELEROMETER")) {
            sensorManager.unregisterListener(this);
            JSONObject sensorEvent = new JSONObject();
            sensorEvent.put("startId", this.requestId);
            sendResult(requestId, sensorEvent);
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
            sendResult(requestId, sensorEvent);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
