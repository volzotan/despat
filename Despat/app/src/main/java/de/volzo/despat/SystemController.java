package de.volzo.despat;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Created by volzotan on 02.12.16.
 */

public class SystemController {

    Context context;

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

        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(activated);
    }

    public void gps(boolean activated) {

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
