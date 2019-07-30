package de.volzo.despat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;


import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by volzotan on 02.12.16.
 */

public class SystemController {

    private static final String TAG = SystemController.class.getSimpleName();

    Context context;

    private float deviceTemperature = -1.0f;

    public SystemController(Context context) {
        this.context = context;
    }

    public void flightmode(boolean activated) {
        // http://stackoverflow.com/questions/13766909

        // is flight mode enabled?
        boolean alreadyEnabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    public void wifi(boolean activated) {
        // http://stackoverflow.com/questions/3930990

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(activated);
    }

    // Location will be available in class variable a few seconds after calling the getLocation method
    public void getLocation(LocationCallback locationCallback) {

        final LocationCallback locCallback = locationCallback;

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "location update");

                locCallback.locationAcquired(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Location Status Changed: " + String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "Location Provider Enabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "Location Provider Disabled: " + provider);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // ?
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Looper looper = null;

        try {
            locationManager.requestSingleUpdate(criteria, locationListener, looper);
        } catch (SecurityException se) {
            Log.e(TAG, "missing ACCESS_LOCATION_FINE permission");
        }
    }

    public void display(boolean activated) {

        // http://stackoverflow.com/questions/30090589

        final Window win = ((MainActivity) context).getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

    }

    public boolean isDisplayOn() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // needs API level >= 21
    public int getBatteryLevel() {
        int batt = -1;

        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        batt = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        if (batt > 0) {
            return batt;
        }

        // alternative for devices with API level < 21 or devices with vendor modifications (e.g. Moto E)

        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level == -1 || scale == -1) {
            return 50;
        }

        return (int) (((float) level / (float) scale) * 100.0f);
    }

    public float getBatteryTemperature() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int output = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        return (float) output / 10f;
    }

    public double getFreeRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMB = mi.availMem / 1048576L;
        double percentAvail = (double) mi.availMem / (double) mi.totalMem;

        return availableMB;
    }

    public boolean getBatteryChargingState() {

//        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        Intent batteryStatus = context.registerReceiver(null, ifilter);
//
//        // Are we charging / charged?
//        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
//
//        // How are we charging?
//        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return batteryManager.isCharging();

    }

    public void reboot() {
//        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        devicePolicyManager.reboot(new ComponentName("de.volzo.despat", "despat")); // TODO

//        PowerManager powerManger = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        powerManger.reboot(null);

//        Shell.SU.run("reboot");
    }

    // TEMP SENSOR

    public boolean hasTemperatureSensor() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (tempSensor != null) {
            return true;
        }

        return false;
    }

    public void startTemperatureMeasurement() {
        final SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                deviceTemperature = event.values[0];

                SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
                sensorManager.unregisterListener(this);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(sensorEventListener, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getTemperature() {
        return deviceTemperature;
    }

    public static abstract class LocationCallback {
        public void locationAcquired(Location location) {
        }
    }
}

