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

    public static final String TAG = Config.class.getSimpleName();

    public static final String DATEFORMAT                       = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String IMAGE_FILEEXTENSION              = ".jpg";

    public static final float IMGROLL_FREE_SPACE_THRESHOLD      = 300; // in MB
    public static final boolean IMGROLL_DELETE_IF_FULL          = false;

    public static final boolean PHONE_HOME                      = true;

    public static final boolean START_CAMERA_ON_ACTIVITY_START  = true;
    public static final boolean CAMERA_CONTROLLER_RELEASE_EARLY = true;
    public static final int NUMBER_OF_BURST_IMAGES              = 2;
    public static final long WAKELOCK_MAX_LIFETIME              = 5000;
    public static final long SHUTTER_SERVICE_MAX_LIFETIME       = 4000;

    public static final String ACRA_REPORT_URL                  = "http://zoltep.de/report";

    public static final boolean BACKUP_LOGCAT                   = true;
    public static final File LOGCAT_DIR                         = new File(Environment.getExternalStorageDirectory(), ("despat"));
    public static final String DATEFORMAT_LOGFILE               = "yyyy-MM-dd"; // only used for file name

    public static final boolean REBOOT_ON_CRITICAL_ERROR        = false;

    // DEFAULT_SHUTTER_INTERVAL should not be shorter than 6s (5s is android minimum
    // and a few extra ms are needed for compensation of scheduling irregularities)
    private static final long DEFAULT_SHUTTER_INTERVAL          = 10 * 1000; // in ms
    private static final long DEFAULT_HEARTBEAT_INTERVAL        = 15 * 60 * 1000L; // Minimum interval is 15m
    private static final long DEFAULT_UPLOAD_INTERVAL           = 15 * 60 * 1000L;
    private static final File DEFAULT_IMAGE_FOLDER              = new File(Environment.getExternalStorageDirectory(), ("despat"));
    private static final String DEFAULT_SERVER_ADDRESS          = "http://zoltep.de"; // format protocol://example.com
    private static final boolean DEFAULT_RESUME_AFTER_REBOOT    = false;

    private static final String SHAREDPREFNAME                  = "de.volzo.despat.DEFAULT_PREFERENCES";

    public static final String KEY_DEVICENAME                   = "de.volzo.despat.deviceName";
    public static final String KEY_SHUTTER_INTERVAL             = "de.volzo.despat.shutterInterval";
    public static final String KEY_IMAGE_FOLDER                 = "de.volzo.despat.imageFolder";
    public static final String KEY_PHONE_HOME                   = "de.volzo.despat.phoneHome";
    public static final String KEY_SERVER_ADDRESS               = "de.volzo.despat.serverAddress";
    public static final String KEY_HEARTBEAT_INTERVAL           = "de.volzo.despat.heartbeatInterval";
    public static final String KEY_UPLOAD_INTERVAL              = "de.volzo.despat.uploadInterval";
    public static final String KEY_RESUME_AFTER_REBOOT          = "de.volzo.despat.resumeAfterReboot";

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

    public static void init(Context context) {

        // ...
    }

    public static String getUniqueDeviceId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID).toUpperCase();
    }

//    public static void resetImagesTaken(Context context) {
//        setImagesTaken(context, 0);
//    }
//
//    public static void setImagesTaken(Context context, int count) {
//        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putInt("imagesTaken", count);
//        editor.apply();
//    }
//
//    public static int getImagesTaken(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
//        return settings.getInt("imagesTaken", 0);
//    }

    // ----

    public static String sanityCheckDeviceName(Context context) {
        return null;
    }

    public static String sanityCheckShutterInterval(Context context) {
        long value = getShutterInterval(context);

        if (value < 6000) {
            return "Shutter interval is shorter than 6000ms";
        }

        if (value > 6 * 60 * 1000) {
            return "Shutter interval is longer than 6 minutes";
        }

        return null;
    }

    public static String sanityCheckImageFolder(Context context) {
        File imageFolder = getImageFolder(context);

        // check if all folders are existing
        if (!imageFolder.isDirectory()) {
            // not existing. create
            Log.i(TAG, "Directory IMAGE_FOLDER ( " + imageFolder.getAbsolutePath() + " ) missing. creating...");
            imageFolder.mkdirs();
        }

        return null; // TODO: add checks for writability, etc...
    }

    public static String sanityCheckServerAddress(Context context) {
        String value = getServerAddress(context);

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return null;
        }

        return "not a valid URL. Should start with \"http://\" or \"https://\"";
    }

    public static String sanityCheckHeartbeatInterval(Context context) {
        long value = getHeartbeatInterval(context);

        if (value < 15 * 60 * 1000) {
            return "Heartbeat interval is shorter than 15 minutes";
        }

        return null;
    }

    public static String sanityCheckUploadInterval(Context context) {
        long value = getUploadInterval(context);

        if (value < 15 * 60 * 1000) {
            return "Image upload interval is shorter than 15 minutes";
        }

        return null;
    }

    // ----

    private static void setProperty(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static void setProperty(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static String getProperty(Context context, String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    private static int getPropertyInt(Context context, String key, int defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    private static long getPropertyLong(Context context, String key, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    private static boolean getPropertyBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    public static String getDeviceName(Context context) {
        return getProperty(context, KEY_DEVICENAME, android.os.Build.MODEL);
    }

    public static void setDeviceName(Context context, String deviceName) {
        setProperty(context, KEY_DEVICENAME, deviceName);
    }

    public static long getShutterInterval(Context context) {
        return getPropertyLong(context, KEY_SHUTTER_INTERVAL, DEFAULT_SHUTTER_INTERVAL);
    }

    public static void setShutterInterval(Context context, String shutterInterval) {
        setProperty(context, KEY_SHUTTER_INTERVAL, shutterInterval);
    }

    public static File getImageFolder(Context context) {
        return new File(getProperty(context, KEY_IMAGE_FOLDER, DEFAULT_IMAGE_FOLDER.getAbsolutePath()));
    }

    public static void setImageFolder(Context context, String imageFolder) {
        setProperty(context, KEY_IMAGE_FOLDER, imageFolder);
    }

    public static String getServerAddress(Context context) {
        return getProperty(context, KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
    }

    public static void setServerAddress(Context context, String serverAddress) {
        setProperty(context, KEY_SERVER_ADDRESS, serverAddress);
    }

    public static long getHeartbeatInterval(Context context) {
        return getPropertyLong(context, KEY_HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL);
    }

    public static void setHeartbeatInterval(Context context, String heartbeatInterval) {
        setProperty(context, KEY_HEARTBEAT_INTERVAL, heartbeatInterval);
    }

    public static long getUploadInterval(Context context) {
        return getPropertyLong(context, KEY_UPLOAD_INTERVAL, DEFAULT_UPLOAD_INTERVAL);
    }

    public static void setUploadInterval(Context context, String uploadInterval) {
        setProperty(context, KEY_UPLOAD_INTERVAL, uploadInterval);
    }

    public static boolean getResumeAfterReboot(Context context) {
        return getPropertyBoolean(context, KEY_RESUME_AFTER_REBOOT, DEFAULT_RESUME_AFTER_REBOOT);
    }

    public static void setResumeAfterReboot(Context context, boolean resumeAfterReboot) {
        setProperty(context, KEY_UPLOAD_INTERVAL, resumeAfterReboot);
    }
}
