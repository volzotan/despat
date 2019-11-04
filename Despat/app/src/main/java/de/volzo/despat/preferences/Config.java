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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 20.12.16.
 */

public class Config {

    private static final String TAG = Config.class.getSimpleName();

    public static final String DATEFORMAT                           = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATEFORMAT_SHORT                     = "yyyy-MM-dd HH:mm:ss";
    public static final String IMAGE_FILEEXTENSION                  = ".jpg";

    public static final float IMGROLL_FREE_SPACE_THRESHOLD_DELETE   = 300; // in MB
    public static final boolean IMGROLL_DELETE_IF_FULL              = false;

    public static final float IMGROLL_FREE_SPACE_THRESHOLD_SWITCH   = 1024 * 1024 * 1000f; // in Bytes

    public static final String SYNC_AUTHORITY                       = "de.volzo.despat.web.provider";
    public static final String SYNC_ACCOUNT_TYPE                    = "de.volzo.despat.servpat";
    public static final String SYNC_ACCOUNT                         = "despatSync";

    public static final Locale LOCALE                               = Locale.UK;

    // ---------------------------------------------------------------------------------------------

    // display a preview on the main activity
    public static final boolean START_CAMERA_ON_ACTIVITY_START      = true;

    // close the camera without cancelling the
    // AF requests | v2 only
    public static final boolean END_CAPTURE_WITHOUT_UNLOCKING_FOCUS = true;

    // delay between starting AE measurement
    // and release | v1 only
    public static final int SHUTTER_RELEASE_DELAY                   = 500;

    // number of images taken during every capture
    public static final int NUMBER_OF_BURST_IMAGES                  = 1;

    // use fixed ISO value. null=disabled | v1 only
    public static final Integer FIXED_ISO_VALUE                     = 200;

    // set the JPEG image quality | v2 only
    public static final byte JPEG_QUALITY                           = 75;

    // maximal time the AF/AE/AWB metering functions
    // may try to find a fix before shutter is
    // released anyway
    // if v2 is used on a legacy device, this time is
    // always used fully for metering
    public static final int METERING_MAX_TIME                       = 2000;

    public static final boolean BROADCAST_PREVIEW_DETAILS           = false;

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

    // ---SHUTTER SERVICE---------------------------------------------------------------------------

    public static final long CAMERA_RESTART_TIME_WINDOW             = 60 * 1000;

    public static final long CAMERA_RESTART_MAX_NUMBER              = 8;

    public static final int MIN_SHUTTER_INTERVAL                    = 2;    // in s
    public static final int MAX_SHUTTER_INTERVAL                    = 240;  // in s

    // ---------------------------------------------------------------------------------------------

    public static final String ACRA_REPORT_URL                      = "http://zoltep.de/report";

    // copy logcat output and write to file
    public static final boolean BACKUP_LOGCAT                       = false;

    // redirect logcat output via -f to file
    public static final boolean REDIRECT_LOGCAT                     = true;

    // logcat text file directory
    public static final File LOGCAT_DIR                             = new File(Environment.getExternalStorageDirectory(), "despat");

    // TODO: change that to app data directory, ie: /data/data/de.volzo.despat/tmp

    // dateformat used for the logcat file name
    public static final String DATEFORMAT_LOGFILE                   = "yyyy-MM-dd_HH-mm-ss.SSS";

    // reboots the device if sudo rights are granted and a
    // critical error happened (usually the camera)
    public static final boolean REBOOT_ON_CRITICAL_ERROR            = false;

    // stops the running recording session if battery drops
    // below threshold
    public static final float STOP_SESSION_AT_LOW_BATT_THRESHOLD    = 3;


    // ---------------------------------------------------------------------------------------------

    public static final String  DEFAULT_DEVICE_NAME                         = android.os.Build.MODEL;
    public static final boolean DEFAULT_RESUME_AFTER_REBOOT                 = true;
    public static final boolean DEFAULT_SHOW_TOOLTIPS                       = true;
    public static final boolean DEFAULT_ENABLE_RECOGNITION_SERVICE          = true;
    public static final boolean DEFAULT_ENABLE_COMPRESSOR_SERVICE           = true;
    public static final boolean DEFAULT_DELETE_AFTER_RECOGNITION            = true;
    public static final File    DEFAULT_WORKING_DIRECTORY                   = null;
    public static final File    DEFAULT_TEMP_DIRECTORY                      = null;

    public static final String  DEFAULT_CAMERA_DEVICE                       = "0";
    public static final boolean DEFAULT_PERSISTENT_CAMERA                   = true;
    public static final boolean DEFAULT_LEGACY_CAMERA_CONTROLLER            = false;
    public static final int     DEFAULT_SHUTTER_INTERVAL                    = 10 * 1000; // in ms

    // jpegs and/or raw | v2 only
    public static final boolean DEFAULT_FORMAT_JPG                          = true;
    public static final boolean DEFAULT_FORMAT_RAW                          = false;

    public static final int     DEFAULT_EXPOSURE_COMPENSATION               = 0;
    public static final int     DEFAULT_SECOND_IMAGE_EXPOSURE_COMPENSATION  = 0;
    public static final float   DEFAULT_EXPOSURE_THRESHOLD                  = 10f;

    public static final String  DEFAULT_NETWORK_FIDELITY                    = DetectorTensorFlowMobile.FIDELITY_MODE[0];
    public static final boolean DEFAULT_PHONE_HOME                          = false;
    public static final String  DEFAULT_SERVER_ADDRESS                      = "http://zoltep.de";   // format protocol://example.com
    public static final long    DEFAULT_MIN_SYNC_INTERVAL                   = 5 * 60 * 1000;        // at most every X ms
    public static final long    DEFAULT_HEARTBEAT_INTERVAL                  = 15 * 60 * 1000L;      // Minimum interval is 15m
    public static final long    DEFAULT_MIN_HEARTBEAT_INTERVAL              = 2 * 60 * 1000;        // at most every X ms
    public static final long    DEFAULT_UPLOAD_INTERVAL                     = 60 * 60 * 1000L;
    public static final long    DEFAULT_LOCATION_INTERVAL                   = 60 * 60 * 1000L;

    /**
     * DEVICE NAME
     */
    public static final String KEY_DEVICE_NAME                      = "de.volzo.despat.deviceName";
    public static String getDeviceName(Context context) {
        return getProperty(context, KEY_DEVICE_NAME, DEFAULT_DEVICE_NAME);
    }
    public static void setDeviceName(Context context, String deviceName) {
        setProperty(context, KEY_DEVICE_NAME, deviceName);
    }

    /**
     * RESUME AFTER REBOOT
     */
    public static final String KEY_RESUME_AFTER_REBOOT              = "de.volzo.despat.resumeAfterReboot";
    public static boolean getResumeAfterReboot(Context context) {
        return getPropertyBoolean(context, KEY_RESUME_AFTER_REBOOT, DEFAULT_RESUME_AFTER_REBOOT);
    }
    public static void setResumeAfterReboot(Context context, boolean resumeAfterReboot) {
        setProperty(context, KEY_RESUME_AFTER_REBOOT, resumeAfterReboot);
    }

    /**
     * SHOW TOOLTIPS
     */
    public static final String KEY_SHOW_TOOLTIPS                    = "de.volzo.despat.showTooltips";
    public static boolean getShowTooltips(Context context) {
        return getPropertyBoolean(context, KEY_RESUME_AFTER_REBOOT, DEFAULT_RESUME_AFTER_REBOOT);
    }
    public static void setShowTooltips(Context context, boolean showTooltips) {
        setProperty(context, KEY_SHOW_TOOLTIPS, showTooltips);
    }

    /**
     * ENABLE RECOGNITION SERVICE
     */
    public static final String KEY_ENABLE_RECOGNITION_SERVICE        = "de.volzo.despat.enableRecognitionService";
    public static boolean getEnableRecognitionService(Context context) {
        return getPropertyBoolean(context, KEY_ENABLE_RECOGNITION_SERVICE, DEFAULT_ENABLE_RECOGNITION_SERVICE);
    }
    public static void setEnableRecognitionService(Context context, boolean enableRecognitionService) {
        setProperty(context, KEY_ENABLE_RECOGNITION_SERVICE, enableRecognitionService);
    }

    /**
     * ENABLE COMPRESSOR SERVICE
     */
    public static final String KEY_ENABLE_COMPRESSOR_SERVICE        = "de.volzo.despat.enableCompressorService";
    public static boolean getEnableCompressorService(Context context) {
        return getPropertyBoolean(context, KEY_ENABLE_COMPRESSOR_SERVICE, DEFAULT_ENABLE_COMPRESSOR_SERVICE);
    }
    public static void setEnableCompressorService(Context context, boolean enableCompressorService) {
        setProperty(context, KEY_ENABLE_COMPRESSOR_SERVICE, enableCompressorService);
    }

    /**
     * DELETE AFTER RECOGNITION
     */
    public static final String KEY_DELETE_AFTER_RECOGNITION          = "de.volzo.despat.deleteAfterRecognition";
    public static boolean getDeleteAfterRecognition(Context context) {
        return getPropertyBoolean(context, KEY_DELETE_AFTER_RECOGNITION, DEFAULT_DELETE_AFTER_RECOGNITION);
    }
    public static void setDeleteAfterRecognition(Context context, boolean deleteAfterRecognition) {
        setProperty(context, KEY_DELETE_AFTER_RECOGNITION, deleteAfterRecognition);
    }

    /**
     * WORKING DIRECTORY
     */
    public static final String KEY_WORKING_DIRECTORY                = "de.volzo.despat.workingDirectory";
    public static final File getWorkingDirectory(Context context) {
        String fh = getProperty(context, KEY_WORKING_DIRECTORY, "");
        if (fh == null || fh.equals("")) {
            if (DEFAULT_WORKING_DIRECTORY != null) {
                return DEFAULT_WORKING_DIRECTORY;
            } else {
                return new File(Environment.getExternalStorageDirectory(), "despat");
//                return new File(context.getApplicationInfo().dataDir);
            }
        }
        return new File(fh);
    }

    /**
     * TEMP DIRECTORY
     *
     * Session export temp directory
     */
    public static final String KEY_TEMP_DIRECTORY                   = "de.volzo.despat.tempDirectory";
    public static final File getTempDirectory(Context context) {
        String fh = getProperty(context, KEY_TEMP_DIRECTORY, "");
        if (fh == null || fh.equals("")) {
            if (DEFAULT_TEMP_DIRECTORY != null) {
                return DEFAULT_TEMP_DIRECTORY;
            } else {
                return new File(Environment.getExternalStorageDirectory(), "despat/tmp");
//                return new File(context.getApplicationInfo().dataDir, "tmp");
            }
        }
        return new File(fh);
    }

    /**
     * CAMERA_DEVICE
     *
     * Choose the Camera Device which is used for preview and capturing.
     * Relevant phones with several back cameras.
     */
    public static final String KEY_CAMERA_DEVICE                    = "de.volzo.despat.cameraDevice";
    public static final String getCameraDevice(Context context) {
        return getProperty(context, KEY_CAMERA_DEVICE, DEFAULT_CAMERA_DEVICE);
    }
    public static void setPersistentCamera(Context context, String cameraDevice) {
        setProperty(context, KEY_CAMERA_DEVICE, cameraDevice);
    }

    /**
     * PERSISTENT CAMERA
     *
     * keep the camera alive permanently and do not close and reinit
     * for every capture.
     * Does not allow the device to sleep in between captures
     */
    public static final String KEY_PERSISTENT_CAMERA                = "de.volzo.despat.persistentCamera";
    public static final boolean getPersistentCamera(Context context) {
        return getPropertyBoolean(context, KEY_PERSISTENT_CAMERA, DEFAULT_PERSISTENT_CAMERA);
    }
    public static void setPersistentCamera(Context context, boolean persistentCamera) {
        setProperty(context, KEY_PERSISTENT_CAMERA, persistentCamera);
    }

    /**
     * LEGACY CAMERA CONTROLLER
     */
    public static final String KEY_LEGACY_CAMERA_CONTROLLER         = "de.volzo.despat.legacyCameraController";
    public static final boolean getLegacyCameraController(Context context) {
        return getPropertyBoolean(context, KEY_LEGACY_CAMERA_CONTROLLER, DEFAULT_LEGACY_CAMERA_CONTROLLER);
    }

    /**
     * SHUTTER INTERVAL
     *
     * if PERSISTENT_CAMERA is _disabled_ DEFAULT_SHUTTER_INTERVAL should not be
     * shorter than 6s (5s is android minimum and a few extra ms are needed for
     * compensation of scheduling irregularities)
     * if PERSISTENT_CAMERA is _enabled_ DEFAULT_SHUTTER_INTERVAL can be shorter than 6s.
     */
    public static final String KEY_SHUTTER_INTERVAL                 = "de.volzo.despat.shutterInterval";
    public static int getShutterInterval(Context context) {
        return getPropertyInt(context, KEY_SHUTTER_INTERVAL, DEFAULT_SHUTTER_INTERVAL);
    }
    public static void setShutterInterval(Context context, int shutterInterval) {
        setProperty(context, KEY_SHUTTER_INTERVAL, shutterInterval);
    }

    /**
     * FORMAT JPG
     *
     * take a JPEG picture | v2 only
     */
    public static final String KEY_FORMAT_JPG                       = "de.volzo.despat.formatJpg";
    public static final boolean getFormatJpg(Context context) {
        return getPropertyBoolean(context, KEY_FORMAT_JPG, DEFAULT_FORMAT_JPG);
    }

    /**
     * FORMAT RAW
     *
     * take a RAW picture | v2 only
     */
    public static final String KEY_FORMAT_RAW                       = "de.volzo.despat.formatRaw";
    public static final boolean getFormatRaw(Context context) {
        return getPropertyBoolean(context, KEY_FORMAT_RAW, DEFAULT_FORMAT_RAW);
    }


    /**
     * EXPOSURE COMPENSATION
     *
     * over- or underexposure compensation
     *
     */
    public static final String KEY_EXPOSURE_COMPENSATION            = "de.volzo.despat.exposureCompensation";
    public static int getExposureCompensation(Context context) {
        return Integer.parseInt(getProperty(context, KEY_EXPOSURE_COMPENSATION, Integer.toString(DEFAULT_EXPOSURE_COMPENSATION)));
    }
    public static void setExposureCompensation(Context context, int exposureCompensation) {
        setProperty(context, KEY_EXPOSURE_COMPENSATION, Integer.toString(exposureCompensation));
    }

    /**
     * SECOND IMAGE EXPOSURE COMPENSATION
     *
     * over- or underexposure compensation applied to a second image
     * (no second image is taken when value == 0) | v2 only
     *
     */
    public static final String KEY_SECOND_IMAGE_EXPOSURE_COMPENSATION   = "de.volzo.despat.secondImageExposureCompensation";
    public static int getSecondImageExposureCompensation(Context context) {
        return Integer.parseInt(getProperty(context, KEY_SECOND_IMAGE_EXPOSURE_COMPENSATION, Integer.toString(DEFAULT_SECOND_IMAGE_EXPOSURE_COMPENSATION)));
    }
    public static void setSecondImageExposureCompensation(Context context, int secondImageExposureCompensation) {
        setProperty(context, KEY_SECOND_IMAGE_EXPOSURE_COMPENSATION, Integer.toString(secondImageExposureCompensation));
    }

    /**
     * EXPOSURE THRESHOLD
     *
     * exposure value must be less than threshold to trigger 2nd capture
     * (disabled if threshold is 0) | v2 only
     */
    public static final String KEY_EXPOSURE_THRESHOLD               = "de.volzo.despat.exposureThreshold";
    public static float getExposureThreshold(Context context) {
        return getPropertyInt(context, KEY_EXPOSURE_THRESHOLD, Math.round(DEFAULT_EXPOSURE_THRESHOLD));
    }
    public static void setExposureThreshold(Context context, float exposureThreshold) {
        setProperty(context, KEY_EXPOSURE_THRESHOLD, exposureThreshold);
    }

    /**
     * NETWORK FIDELITY
     */
    public static final String KEY_NETWORK_FIDELITY                 = "de.volzo.despat.networkFidelity";
    public static String getNetworkFidelity(Context context) {
        return getProperty(context, KEY_NETWORK_FIDELITY, DEFAULT_NETWORK_FIDELITY);
    }
    public static void setNetworkFidelity(Context context, int networkFidelity) {
        setProperty(context, KEY_NETWORK_FIDELITY, networkFidelity);
    }

    /**
     * PHONE HOME
     */
    public static final String KEY_PHONE_HOME                       = "de.volzo.despat.phoneHome";
    public static final boolean getPhoneHome(Context context) {
        return getPropertyBoolean(context, KEY_PHONE_HOME, DEFAULT_PHONE_HOME);
    }
    public static void setPhoneHome(Context context, boolean phoneHome) {
        setProperty(context, KEY_PHONE_HOME, phoneHome);
    }

    /**
     * SERVER ADDRESS
     */
    public static final String KEY_SERVER_ADDRESS                   = "de.volzo.despat.serverAddress";
    public static String getServerAddress(Context context) {
        return getProperty(context, KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
    }
    public static void setServerAddress(Context context, String serverAddress) {
        setProperty(context, KEY_SERVER_ADDRESS, serverAddress);
    }

    /**
     * MIN SYNC INTERVAL
     */
    public static final String KEY_MIN_SYNC_INTERVAL                = "de.volzo.despat.minSyncInterval";
    public static long getMinSyncInterval(Context context) {
        return getPropertyLong(context, KEY_MIN_SYNC_INTERVAL, DEFAULT_MIN_SYNC_INTERVAL);
    }
    public static void setMinSyncInterval(Context context, long minSyncInterval) {
        setProperty(context, KEY_MIN_SYNC_INTERVAL, minSyncInterval);
    }

    /**
     * HEARTBEAT INTERVAL
     */
    public static final String KEY_HEARTBEAT_INTERVAL               = "de.volzo.despat.heartbeatInterval";
    public static long getHeartbeatInterval(Context context) {
        return getPropertyLong(context, KEY_HEARTBEAT_INTERVAL, DEFAULT_HEARTBEAT_INTERVAL);
    }
    public static void setHeartbeatInterval(Context context, String heartbeatInterval) {
        setProperty(context, KEY_HEARTBEAT_INTERVAL, heartbeatInterval);
    }

    /**
     * MIN HEARTBEAT INTERVAL
     */
    public static final String KEY_MIN_HEARTBEAT_INTERVAL           = "de.volzo.despat.minHeartbeatInterval";
    public static long getMinHeartbeatInterval(Context context) {
        return getPropertyLong(context, KEY_MIN_HEARTBEAT_INTERVAL, DEFAULT_MIN_HEARTBEAT_INTERVAL);
    }
    public static void setMinHeartbeatInterval(Context context, long minHeartbeatInterval) {
        setProperty(context, KEY_MIN_HEARTBEAT_INTERVAL, minHeartbeatInterval);
    }

    /**
     * UPLOAD INTERVAL
     */
    public static final String KEY_UPLOAD_INTERVAL                  = "de.volzo.despat.uploadInterval";
    public static long getUploadInterval(Context context) {
        return getPropertyLong(context, KEY_UPLOAD_INTERVAL, DEFAULT_UPLOAD_INTERVAL);
    }
    public static void setUploadInterval(Context context, String uploadInterval) {
        setProperty(context, KEY_UPLOAD_INTERVAL, uploadInterval);
    }
    public static String sanityCheckUploadInterval(Context context) {
        long value = getUploadInterval(context);

        if (value < 15 * 60 * 1000) {
            return "Image upload interval is shorter than 15 minutes";
        }

        return null;
    }

    /**
     * LOCATION INTERVAL
     */
    public static final String KEY_LOCATION_INTERVAL                = "de.volzo.despat.locationInterval";
    public static long getLocationInterval(Context context) {
        return getPropertyLong(context, KEY_LOCATION_INTERVAL, DEFAULT_LOCATION_INTERVAL);
    }
    public static void setLocationInterval(Context context, String locationInterval) {
        setProperty(context, KEY_LOCATION_INTERVAL, locationInterval);
    }

    /**
     * KEY LAST SYNC
     */
    public static final String KEY_LAST_SYNC                        = "de.volzo.despat.lastSync";
    public static Date getLastSync(Context context) {
        return getPropertyDate(context, KEY_LAST_SYNC, null);
    }
    public static void setLastSync(Context context, Date lastSync) {
        setPropertyDate(context, KEY_LAST_SYNC, lastSync);
    }

    /**
     * IMAGE FOLDERS
     */
    public static final String KEY_IMAGE_FOLDERS                    = "de.volzo.despat.imageFolders";
    public static List<File> getImageFolders(Context context) {
        List<File> flist = new ArrayList<>();

        String files = getProperty(context, KEY_IMAGE_FOLDERS, null);

        if (files != null && !files.isEmpty()) {
            for (String f : files.split(",")) {
                if (f == null || f.isEmpty()) continue;
                flist.add(new File(f));
            }
        } else { // default image folders

            // main directory on internal storage
            flist.add(new File(getWorkingDirectory(context).getAbsolutePath()));

            // secondary directory on mounted external SD card
            flist.add(Util.getExternalSDcards(context));
        }

        return flist;
    }
    public static void setImageFolder(Context context, List<File> imageFolders) {
        StringBuilder sb = new StringBuilder();

        for (File f : imageFolders) {
            sb.append(f.getAbsolutePath());
            sb.append(",");
        }

        setProperty(context, KEY_IMAGE_FOLDERS, sb.toString());
    }

    /**
     * FIRST TIME LAUNCH
     */
    public static final String KEY_FIRST_TIME_LAUNCH                = "de.volzo.despat.firstTimeLaunch";
    public static boolean getFirstTimeLaunch(Context context) {
        return getPropertyBoolean(context, KEY_FIRST_TIME_LAUNCH, true);
    }
    public static void setFirstTimeLaunch(Context context, boolean firstTimeLaunch) {
        setProperty(context, KEY_FIRST_TIME_LAUNCH, firstTimeLaunch);
    }

    /**
     * NEXT SHUTTER INVOCATION
     */
    public static final String KEY_NEXT_SHUTTER_SERVICE_INVOCATION  = "de.volzo.despat.nextShutterServiceInvocation";
    public static long getNextShutterServiceInvocation(Context context) {
        return getPropertyLong(context, KEY_NEXT_SHUTTER_SERVICE_INVOCATION, -1);
    }
    public static void setNextShutterServiceInvocation(Context context, long timestamp) {
        setProperty(context, KEY_NEXT_SHUTTER_SERVICE_INVOCATION, timestamp);
    }

    // ---------------------------------------------------------------------------------------------

    public static void enableCTMode(Context context) {

        Log.i(TAG, "enabling Compressed Time mode");

        setEnableRecognitionService(context, false);
        Log.i(TAG, "disabling Recognition");

        setDeleteAfterRecognition(context, false);
        Log.i(TAG, "deleting images after recognition is disabled");

        setPersistentCamera(context, false);
        Log.i(TAG, "persistent camera disabled");

        setShutterInterval(context, 10 * 1000);
        Log.i(TAG, "set shutter interval to 10s");

        setExposureCompensation(context, 1);
        Log.i(TAG, "slightly increased exposure compensation (+1)");

        setSecondImageExposureCompensation(context, -10);
        Log.i(TAG, "set second image exposure compensation (-10)");

    }

    // ---------------------------------------------------------------------------------------------

    public static void init(Context context) {
        List<File> imageFolders = Config.getImageFolders(context);

        for (File imageFolder : imageFolders) {
            if (imageFolder != null && !imageFolder.isDirectory()) {
                if (imageFolder.exists()) {
                    Log.wtf(TAG, "Image Folder " + imageFolder.getAbsoluteFile() + " exists but is no directory");
                    return;
                }

                imageFolder.mkdirs();
                Log.i(TAG, "created directory: " + imageFolder.getAbsoluteFile());
            }
        }
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
//        editor.commit();
//    }
//
//    public static int getImagesTaken(Context context) {
//        SharedPreferences settings = context.getSharedPreferences(SHAREDPREFNAME, Context.MODE_PRIVATE);
//        return settings.getInt("imagesTaken", 0);
//    }

    // ----

    private static void setProperty(Context context, String key, String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static void setProperty(Context context, String key, boolean value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void setProperty(Context context, String key, long value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static void setProperty(Context context, String key, int value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static void setProperty(Context context, String key, float value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.commit();
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

    private static float getPropertyFloat(Context context, String key, float defaultValue) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getFloat(key, defaultValue);
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
        editor.commit();
    }

    // ---

    public static void validate(Context context) throws Exception {

        if (IMAGE_FILEEXTENSION == null) throw new Exception("image fileextension missing");

        if (!Config.getFormatJpg(context) && !Config.getFormatRaw(context)) throw new Exception("no image format selected");
        if (DEFAULT_SHUTTER_INTERVAL < 6000) throw new Exception("shutter interval shorter than 6s");

    }

    private static String strip(String inp) {
        String[] segments = inp.split("\\.");
        return segments[segments.length-1];
    }

    public static String print(Context context) {
        StringBuilder sb = new StringBuilder();

        sb.append(" \n");
        sb.append("----------------------------------------\n");

//        sb.append(String.format("%-20s", "IMGROLL_FREE_SPACE_THRESHOLD"));
//        sb.append(String.format("%20f\n", Config.IMGROLL_FREE_SPACE_THRESHOLD));
//
//        sb.append(String.format("%-20s", "IMGROLL_DELETE_IF_FULL"));
//        sb.append(String.format("%20s\n", Config.IMGROLL_DELETE_IF_FULL));

        sb.append(String.format("%-20s", strip(Config.KEY_DEVICE_NAME)));
        sb.append(String.format("%20s\n", getDeviceName(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_RESUME_AFTER_REBOOT)));
        sb.append(String.format("%20s\n", getResumeAfterReboot(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_SHOW_TOOLTIPS)));
        sb.append(String.format("%20s\n", getShowTooltips(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_ENABLE_RECOGNITION_SERVICE)));
        sb.append(String.format("%20s\n", getEnableRecognitionService(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_ENABLE_COMPRESSOR_SERVICE)));
        sb.append(String.format("%20s\n", getEnableCompressorService(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_DELETE_AFTER_RECOGNITION)));
        sb.append(String.format("%20s\n", getDeleteAfterRecognition(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_WORKING_DIRECTORY)));
        sb.append(String.format("%20s\n", getWorkingDirectory(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_PERSISTENT_CAMERA)));
        sb.append(String.format("%20s\n", getPersistentCamera(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_LEGACY_CAMERA_CONTROLLER)));
        sb.append(String.format("%20s\n", getLegacyCameraController(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_SHUTTER_INTERVAL)));
        sb.append(String.format("%20s\n", getShutterInterval(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_EXPOSURE_COMPENSATION)));
        sb.append(String.format("%20s\n", getExposureCompensation(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_SECOND_IMAGE_EXPOSURE_COMPENSATION)));
        sb.append(String.format("%20s\n", getSecondImageExposureCompensation(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_NETWORK_FIDELITY)));
        sb.append(String.format("%20s\n", getNetworkFidelity(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_PHONE_HOME)));
        sb.append(String.format("%20s\n", getPhoneHome(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_SERVER_ADDRESS)));
        sb.append(String.format("%20s\n", getServerAddress(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_MIN_SYNC_INTERVAL)));
        sb.append(String.format("%20s\n", getMinSyncInterval(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_HEARTBEAT_INTERVAL)));
        sb.append(String.format("%20s\n", getHeartbeatInterval(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_MIN_HEARTBEAT_INTERVAL)));
        sb.append(String.format("%20s\n", getMinHeartbeatInterval(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_UPLOAD_INTERVAL)));
        sb.append(String.format("%20s\n", getUploadInterval(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_LAST_SYNC)));
        sb.append(String.format("%20s\n", getLastSync(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_IMAGE_FOLDERS)));
        sb.append(String.format("%20s\n", getImageFolders(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_FIRST_TIME_LAUNCH)));
        sb.append(String.format("%20s\n", getFirstTimeLaunch(context)));

        sb.append(String.format("%-20s", strip(Config.KEY_NEXT_SHUTTER_SERVICE_INVOCATION)));
        sb.append(String.format("%20s\n", getNextShutterServiceInvocation(context)));

        return sb.toString();
    }
}
