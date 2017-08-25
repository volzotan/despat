package de.volzo.despat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by volzotan on 02.12.16.
 */

public class SystemController {

    public static final String TAG = SystemController.class.getName();

    Context context;

    Location deviceLocation;

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
    public void getLocation() {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "location update");
                deviceLocation = location;
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
        win.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );

    }

    public int getBatteryLevel() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public boolean getBatteryChargingState() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return batteryManager.isCharging();
    }
}
