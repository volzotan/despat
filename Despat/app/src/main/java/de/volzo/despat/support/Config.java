package de.volzo.despat.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.provider.Settings.Secure;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by volzotan on 20.12.16.
 */

public class Config {

    public static final String TAG = Config.class.getSimpleName();

    public static final String DATEFORMAT                       = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String IMAGE_FILEEXTENSION              = ".jpg";

    public static final float IMGROLL_FREE_SPACE_THRESHOLD      = 300; // in MB
    public static final boolean IMGROLL_DELETE_IF_FULL          = true;

    public static final boolean PHONE_HOME                      = true;

    public static final String SYNC_AUTHORITY                   = "de.volzo.despat.web.provider";
    public static final String SYNC_ACCOUNT_TYPE                = "de.volzo.despat.servpat";
    public static final String SYNC_ACCOUNT                     = "despatSync";

    // ---------------------------------------------------------------------------------------------

    // use CameraController v1 (old) or v2
    public static final int USE_CAMERA_CONTROLLER               = 1;

    // display a preview on the main activity
    public static final boolean START_CAMERA_ON_ACTIVITY_START  = false;

    // release shutter even without full AF/AE fix | v2 only
    public static final boolean CAMERA_CONTROLLER_RELEASE_EARLY = true;

    // close the camera without cancelling the
    // AF requests | v2 only
    public static final boolean END_CAPTURE_WITHOUT_UNLOCKING_FOCUS = true;

    // delay between having AF/AE fix and release | v2
    // delay between starting AE measurement and release | v1
    public static final int SHUTTER_RELEASE_DELAY               = 500;

    // number of images taken during every capture
    public static final int NUMBER_OF_BURST_IMAGES              = 2;

    // use fixed ISO value. null=disabled | v1 only
    public static final Integer FIXED_ISO_VALUE                 = 200;

    // over- or underexposure compensation | v1 only
    // array position is image number in burst sequence
    // if length==1 every image gets the value of [0]
    public static final int[] EXPOSURE_COMPENSATION             = {0, -12};

    // maximal time the autofocus may try to find a fix
    // before shutter is released anyway | v1 only
    public static final int AUTOFOCUS_MAX_TIME                  = 2000;

    // ---------------------------------------------------------------------------------------------

    // ShutterService closes the camera after X seconds
    public static final long SHUTTER_CAMERA_MAX_LIFETIME        = 6000;

    // ShutterService itself is closed after X seconds
    public static final long SHUTTER_SERVICE_MAX_LIFETIME       = 8000;

    // the maximum time the wakelock is guaranteed to be active
    public static final long WAKELOCK_MAX_LIFETIME              = 9000;

    // ---------------------------------------------------------------------------------------------

    public static final String ACRA_REPORT_URL                  = "http://zoltep.de/report";

    // copy logcat output and write to file
    public static final boolean BACKUP_LOGCAT                   = false;

    // redirect logcat output via -f to file
    public static final boolean REDIRECT_LOGCAT                 = true;

    // logcat text file directory
    public static final File LOGCAT_DIR                         = new File(Environment.getExternalStorageDirectory(), ("despat"));

    // dateformat used for the logcat file name
    public static final String DATEFORMAT_LOGFILE               = "yyyy-MM-dd";

    // reboots the device if sudo rights are granted and a
    // critical error happened (usually the camera)
    public static final boolean REBOOT_ON_CRITICAL_ERROR        = false;

    // ---------------------------------------------------------------------------------------------

    // DEFAULT_SHUTTER_INTERVAL should not be shorter than 6s (5s is android minimum
    // and a few extra ms are needed for compensation of scheduling irregularities)
    private static final long DEFAULT_SHUTTER_INTERVAL          = 10 * 1000; // in ms
    private static final long DEFAULT_HEARTBEAT_INTERVAL        = 15 * 60 * 1000L; // Minimum interval is 15m
    private static final long DEFAULT_UPLOAD_INTERVAL           = 15 * 60 * 1000L;
    private static final File DEFAULT_IMAGE_FOLDER              = new File(Environment.getExternalStorageDirectory(), ("despat"));
    private static final String DEFAULT_SERVER_ADDRESS          = "http://zoltep.de"; // format protocol://example.com
    private static final boolean DEFAULT_RESUME_AFTER_REBOOT    = false;
    private static final long DEFAULT_MIN_SYNC_INTERVAL         = 5 * 60 * 1000;

    private static final String SHAREDPREFNAME                  = "de.volzo.despat.DEFAULT_PREFERENCES";

    public static final String KEY_DEVICENAME                   = "de.volzo.despat.deviceName";
    public static final String KEY_SHUTTER_INTERVAL             = "de.volzo.despat.shutterInterval";
    public static final String KEY_IMAGE_FOLDER                 = "de.volzo.despat.imageFolder";
    public static final String KEY_PHONE_HOME                   = "de.volzo.despat.phoneHome";
    public static final String KEY_SERVER_ADDRESS               = "de.volzo.despat.serverAddress";
    public static final String KEY_HEARTBEAT_INTERVAL           = "de.volzo.despat.heartbeatInterval";
    public static final String KEY_UPLOAD_INTERVAL              = "de.volzo.despat.uploadInterval";
    public static final String KEY_RESUME_AFTER_REBOOT          = "de.volzo.despat.resumeAfterReboot";
    public static final String KEY_LAST_SYNC                    = "de.volzo.despat.lastSync";
    public static final String KEY_MIN_SYNC_INTERVAL            = "de.volzo.despat.minSyncInterval";

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

    private static Date getPropertyDate(Context context, String key, Date defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        String retrievedValue = settings.getString(key, null);
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));

        if (retrievedValue == null || retrievedValue.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return dateFormat.parse(retrievedValue);
            } catch (ParseException e) {
                Log.e(TAG, "parsing stored date failed", e);
                return defaultValue;
            }
        }
    }

    private static void setPropertyDate(Context context, String key, Date value) {
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, dateFormat.format(value));
        editor.apply();
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

    public static Date getLastSync(Context context) {
        return getPropertyDate(context, KEY_LAST_SYNC, null);
    }

    public static void setLastSync(Context context, Date lastSync) {
        setPropertyDate(context, KEY_UPLOAD_INTERVAL, lastSync);
    }

    public static long getMinSyncInterval(Context context) {
        return getPropertyLong(context, KEY_MIN_SYNC_INTERVAL, DEFAULT_MIN_SYNC_INTERVAL);
    }

    public static void setMinSyncInterval(Context context, String minSyncInterval) {
        setProperty(context, KEY_MIN_SYNC_INTERVAL, minSyncInterval);
    }
}
