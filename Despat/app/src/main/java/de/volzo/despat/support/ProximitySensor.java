package de.volzo.despat.support;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ProximitySensor implements SensorEventListener {

    private static final String TAG = ProximitySensor.class.getSimpleName();

    Context context;
    Sensor sensor;

    public ProximitySensor(Context context) {
        this.context = context;

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void close() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        Log.d(TAG, "proximity sensor event. distance: " + distance);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "proximity sensor event. accuracy: " + accuracy);
    }
}