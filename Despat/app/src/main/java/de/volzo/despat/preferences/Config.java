package de.volzo.despat.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.provider.Settings.Secure;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.volzo.despat.R;

/**
 * Created by volzotan on 20.12.16.
 */

public class Config {

    public static final String TAG = Config.class.getSimpleName();

    public static final String DATEFORMAT                           = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String IMAGE_FILEEXTENSION                  = ".jpg";

    public static final float IMGROLL_FREE_SPACE_THRESHOLD          = 300; // in MB
    public static final boolean IMGROLL_DELETE_IF_FULL              = false;

    public static final String SYNC_AUTHORITY                       = "de.volzo.despat.web.provider";
    public static final String SYNC_ACCOUNT_TYPE                    = "de.volzo.despat.servpat";
    public static final String SYNC_ACCOUNT                         = "despatSync";

    // ---------------------------------------------------------------------------------------------

    // use CameraController v1 (old) or v2
    public static final int USE_CAMERA_CONTROLLER                   = 2;

    // jpegs and/or raw | v2 only
    public static final boolean FORMAT_JPG                          = true;
    public static final boolean FORMAT_RAW                          = false;

    // display a preview on the main activity
    public static final boolean START_CAMERA_ON_ACTIVITY_START      = false;

    // close the camera without cancelling the
    // AF requests | v2 only
    public static final boolean END_CAPTURE_WITHOUT_UNLOCKING_FOCUS = true;

    // delay between starting AE measurement
    // and release | v1 only
    public static final int SHUTTER_RELEASE_DELAY                   = 500;

    // number of images taken during every capture
    public static final int NUMBER_OF_BURST_IMAGES                  = 2;

    // use fixed ISO value. null=disabled | v1 only
    public static final Integer FIXED_ISO_VALUE                     = 200;

    // over- or underexposure compensation | v1 only
    public static final int EXPOSURE_COMPENSATION                   = 0;

    // maximal time the AF/AE/AWB metering functions
    // may try to find a fix before shutter is
    // released anyway
    // if v2 is used on a legacy device, this time is
    // always used fully for metering
    public static final int METERING_MAX_TIME                       = 1500;

    // make captured images immediately available to
    // the android gallery app | v2 only
    public static final boolean RUN_MEDIASCANNER_AFTER_CAPTURE      = false;

    // ---PERSISTENT_CAMERA-------------------------------------------------------------------------

    // starts a metering run before every capture.
    // May take up to METERING_MAX_TIME
    public static final boolean RERUN_METERING_BEFORE_CAPTURE       = true;

    // ---!PERSISTENT_CAMERA------------------------------------------------------------------------

    // ShutterService closes the camera after X seconds
    // recommended: SHUTTER_INTERVAL - 1500
    public static final long SHUTTER_CAMERA_MAX_LIFETIME            = 8000;

    // the maximum time the wakelock is guaranteed to be active
    // recommended: SHUTTER_INTERVAL - 1000
    public static final long WAKELOCK_MAX_LIFETIME                  = 9000;

    // ---------------------------------------------------------------------------------------------

    public static final String ACRA_REPORT_URL                      = "http://zoltep.de/report";

    // copy logcat output and write to file
    public static final boolean BACKUP_LOGCAT                       = false;

    // redirect logcat output via -f to file
    public static final boolean REDIRECT_LOGCAT                     = false;

    // logcat text file directory
    public static final File LOGCAT_DIR                             = new File(Environment.getExternalStorageDirectory(), ("despat"));

    // dateformat used for the logcat file name
    public static final String DATEFORMAT_LOGFILE                   = "yyyy-MM-dd";

    // reboots the device if sudo rights are granted and a
    // critical error happened (usually the camera)
    public static final boolean REBOOT_ON_CRITICAL_ERROR            = false;

    // ---------------------------------------------------------------------------------------------

    public static final String DEFAULT_DEVICE_NAME                  = android.os.Build.MODEL;
    public static final boolean DEFAULT_RESUME_AFTER_REBOOT         = true;
    public static final File DEFAULT_WORKING_DIRECTORY              = new File(Environment.getExternalStorageDirectory(), ("despat"));

    // keep the camera alive permanently and do not close and reinit
    // for every capture.
    // Does not allow the device to sleep in between captures
    public static final boolean DEFAULT_PERSISTENT_CAMERA           = true;
    public static final boolean DEFAULT_LEGACY_CAMERA_CONTROLLER    = false;
    // if PERSISTENT_CAMERA is _disabled_ DEFAULT_SHUTTER_INTERVAL should not be
    // shorter than 6s (5s is android minimum and a few extra ms are needed for
    // compensation of scheduling irregularities)
    // if PERSISTENT_CAMERA is _enabled_ DEFAULT_SHUTTER_INTERVAL can be shorter than 6s.
    public static final int DEFAULT_SHUTTER_INTERVAL                = 10 * 1000; // in ms

    public static final boolean DEFAULT_PHONE_HOME                  = false;
    public static final String DEFAULT_SERVER_ADDRESS               = "http://zoltep.de";   // format protocol://example.com
    public static final long DEFAULT_MIN_SYNC_INTERVAL              = 5 * 60 * 1000;        // at most every X ms
    public static final long DEFAULT_HEARTBEAT_INTERVAL             = 15 * 60 * 1000L;      // Minimum interval is 15m
    public static final long DEFAULT_UPLOAD_INTERVAL                = 15 * 60 * 1000L;


    public static final String KEY_DEVICE_NAME                      = "de.volzo.despat.deviceName";
    public static final String KEY_RESUME_AFTER_REBOOT              = "de.volzo.despat.resumeAfterReboot";
    public static final String KEY_WORKING_DIRECTORY                = "de.volzo.despat.workingDirectory";

    public static final String KEY_PERSISTENT_CAMERA                = "de.volzo.despat.persistentCamera";
    public static final String KEY_LEGACY_CAMERA_CONTROLLER         = "de.volzo.despat.legacyCameraController";
    public static final String KEY_SHUTTER_INTERVAL                 = "de.volzo.despat.shutterInterval";

    public static final String KEY_PHONE_HOME                       = "de.volzo.despat.phoneHome";
    public static final String KEY_SERVER_ADDRESS                   = "de.volzo.despat.serverAddress";
    public static final String KEY_MIN_SYNC_INTERVAL                = "de.volzo.despat.minSyncInterval";
    public static final String KEY_HEARTBEAT_INTERVAL               = "de.volzo.despat.heartbeatInterval";
    public static final String KEY_UPLOAD_INTERVAL                  = "de.volzo.despat.uploadInterval";

    public static final String KEY_LAST_SYNC                        = "de.volzo.despat.lastSync";
    public static final String KEY_IMAGE_FOLDER                     = "de.volzo.despat.imageFolder";
    public static final String KEY_NEXT_SHUTTER_SERVICE_INVOCATION  = "de.volzo.despat.nextShutterServiceInvocation";






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

    }

    public static void reset(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static void setProperty(Context context, String key, boolean value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static void setProperty(Context context, String key, long value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private static String getProperty(Context context, String key, String defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(key, defaultValue);
    }

    private static int getPropertyInt(Context context, String key, int defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(key, defaultValue);
    }

    private static long getPropertyLong(Context context, String key, long defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getLong(key, defaultValue);
    }

    private static boolean getPropertyBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(key, defaultValue);
    }

    private static Date getPropertyDate(Context context, String key, Date defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, dateFormat.format(value));
        editor.apply();
    }

    // ---

    public static String getDeviceName(Context context) {
        return getProperty(context, KEY_DEVICE_NAME, android.os.Build.MODEL);
    }

    public static void setDeviceName(Context context, String deviceName) {
        setProperty(context, KEY_DEVICE_NAME, deviceName);
    }

    public static final boolean getPersistentCamera(Context context) {
        return getPropertyBoolean(context, KEY_PERSISTENT_CAMERA, DEFAULT_PERSISTENT_CAMERA);
    }

    public static long getShutterInterval(Context context) {
        return getPropertyLong(context, KEY_SHUTTER_INTERVAL, DEFAULT_SHUTTER_INTERVAL);
    }

    public static void setShutterInterval(Context context, long shutterInterval) {
        setProperty(context, KEY_SHUTTER_INTERVAL, shutterInterval);
    }

    public static File getImageFolder(Context context) {
        return new File(getProperty(context, KEY_IMAGE_FOLDER, DEFAULT_WORKING_DIRECTORY.getAbsolutePath()));
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
        setProperty(context, KEY_RESUME_AFTER_REBOOT, resumeAfterReboot);
    }

    public static Date getLastSync(Context context) {
        return getPropertyDate(context, KEY_LAST_SYNC, null);
    }

    public static void setLastSync(Context context, Date lastSync) {
        setPropertyDate(context, KEY_LAST_SYNC, lastSync);
    }

    public static long getMinSyncInterval(Context context) {
        return getPropertyLong(context, KEY_MIN_SYNC_INTERVAL, DEFAULT_MIN_SYNC_INTERVAL);
    }

    public static void setMinSyncInterval(Context context, long minSyncInterval) {
        setProperty(context, KEY_MIN_SYNC_INTERVAL, minSyncInterval);
    }

    public static long getNextShutterServiceInvocation(Context context) {
        return getPropertyLong(context, KEY_NEXT_SHUTTER_SERVICE_INVOCATION, -1);
    }

    public static void setNextShutterServiceInvocation(Context context, long timestamp) {
        setProperty(context, KEY_NEXT_SHUTTER_SERVICE_INVOCATION, timestamp);
    }

    public static void validate() throws Exception {

        if (IMAGE_FILEEXTENSION == null) throw new Exception("image fileextension missing");

        if (!Config.FORMAT_JPG && !Config.FORMAT_RAW) throw new Exception("no image format selected");
        if (DEFAULT_SHUTTER_INTERVAL < 6000) throw new Exception("shutter interval shorter than 6s");


    }
}
