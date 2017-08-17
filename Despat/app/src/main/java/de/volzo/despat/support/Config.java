package de.volzo.despat.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.provider.Settings.Secure;

import java.io.File;

/**
 * Created by volzotan on 20.12.16.
 */

public class Config {

    public static final String TAG = Config.class.getName();

    public static long SHUTTER_INTERVAL             = 6000;

    public static final File IMAGE_FOLDER           = new File(Environment.getExternalStorageDirectory(), ("despat"));
    public static final boolean PHONE_HOME          = true;
    public static final String SERVER_ADDRESS       = "http://grinzold.de:5000";

    public static final String dateFormat           = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

    private static final String SHAREDPREFNAME      = "de.volzo.despat.DEFAULT_PREFERENCES";

    /*
    image folder
    store images?
    image rollover number

    powerbrain
        usb device id
        baud rate

    intervals
        image taking interval
        server reporting interval

    */

    public static void init() {
        // check if all folders are existing
        if (!IMAGE_FOLDER.isDirectory()) {
            // not existing. create
            Log.i(TAG, "Directory IMAGE_FOLDER ( " + IMAGE_FOLDER.getAbsolutePath() + " ) missing. creating...");
            IMAGE_FOLDER.mkdirs();
        }

        // ...
    }

    public static String getUniqueDeviceId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    public static void setDeviceName(Context context, String deviceName) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("deviceName", deviceName);
        editor.apply();
    }

    public static String getDeviceName(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        return settings.getString("deviceName", android.os.Build.MODEL);
    }

}
