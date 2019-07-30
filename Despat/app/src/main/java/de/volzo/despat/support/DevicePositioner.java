package de.volzo.despat.support;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.concurrent.Callable;

import static android.content.Context.SENSOR_SERVICE;

public class DevicePositioner implements SensorEventListener, Callable<Integer> {

    private static final String TAG = DevicePositioner.class.getSimpleName();

    Context context;

    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] gravity;
    float[] geomagnetic;

    Integer orientation = null;

    public DevicePositioner(Context context) {
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void close() {
        sensorManager.unregisterListener(this);
    }

    // taken partly from: https://stackoverflow.com/questions/33101488/
    private int getRotationFromAccelerometerOnly(float[] g) {
        double normOfG = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);
        // Normalize the accelerometer vector
        g[0] = (float) (g[0] / normOfG);
        g[1] = (float) (g[1] / normOfG);
        g[2] = (float) (g[2] / normOfG);
        int inclination = (int) Math.round(Math.toDegrees(Math.acos(g[2])));
        int rotation;
        if (inclination < 25 || inclination > 155) {
            // device is flat, return 0
            return 0;
        } else {
            // device is not flat
            rotation = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[1])));
        }

        if (rotation > -45 && rotation <=45) {
            return 0; //return 90;
        }

        if (rotation > 45 && rotation <= 135) {
            return 0;
        }

        if (rotation > -135 && rotation <= -45) {
            return 180;
        }

        if (rotation <= -135 && rotation > 135) {
            return 0; //return 270;
        }

        Log.wtf(TAG, "Accelerometer illegal state");
        return 0;
    }

    private Integer calculateOrientation(float azimuth, float pitch, float roll) {
        // flat on table

        if (Math.abs(pitch) > 1.0 && pitch < 0) {
            Log.d(TAG, "portrait");
            return 90;
        }

        if (Math.abs(pitch) > 1.0 && pitch > 0) {
            Log.d(TAG, "portrait reverse");
            return 270;
        }

        if (Math.abs(roll) > 0.5 && roll < 0) {
            Log.d(TAG, "landscape");
            return 0;
        }

        if (Math.abs(roll) > 0.5 && roll > 0) {
            Log.d(TAG, "landscape reverse");
            return 180;
        }

        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
//        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
//            geomagnetic = event.values;
//        if (gravity != null && geomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
//            if (success) {
//                float data[] = new float[3];
//                SensorManager.getOrientation(R, data);
//                // data contains: azimuth, pitch and roll
//                orientation = calculateOrientation(data[0], data[1], data[2]);
//                Log.d(TAG, "positioner running on gyro [" + orientation + "]");
//                close();
//            }
//            return;
//        }

        if (gravity != null) {
            orientation = getRotationFromAccelerometerOnly(gravity);
            Log.d(TAG, "positioner running on accelerometer only [" + orientation + "]");

            // let it run just once
            close();
        }
    }

    public Integer getOrientation() {
        return orientation;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public Integer call() throws Exception {
        while (orientation == null) {
            Thread.sleep(10);
        }
        close();
        return orientation;
    }
}
