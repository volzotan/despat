package de.volzo.despat;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.Window;
import android.view.WindowManager;

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
}
