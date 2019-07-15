package de.volzo.despat.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ObbInfo;
import android.content.res.ObbScanner;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.BuildConfig;
import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.MainActivity;
import de.volzo.despat.R;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.ErrorEvent;
import de.volzo.despat.persistence.ErrorEventDao;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.services.ShutterService;

import static android.content.Context.ACCOUNT_SERVICE;

/**
 * Created by volzotan on 16.08.17.
 */

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }

        return null;
    }

    public static long getFreeSpaceOnDevice(File dir) {
        try {
            return (new StatFs(dir.getPath())).getAvailableBytes();
        } catch (NullPointerException | IllegalArgumentException e) {
            // either the path is invalid or there is absolutely no space left
            if (dir != null) {
                Log.e(TAG, "free space could not be determined. path: " + dir.getAbsolutePath());
            } else {
                Log.e(TAG, "free space could not be determined. path is null");
            }
            return -1;
        }
    }


    public static float getFreeSpaceOnDeviceInMb(File dir) {
        long freeSpace = getFreeSpaceOnDevice(dir);
        return freeSpace > 0 ? freeSpace / (1024f * 1024f) : -1;
    }

    public static File getExternalSDcards(Context context) {
        File[] externalDirs = ContextCompat.getExternalFilesDirs(context, null);

        // TODO: check for readability

        // TODO: do not return /storage/SDCARDNUMBER/Android/de.volzo.despat/files but /storage/SDCARDNUMBER/despat

        if (externalDirs.length > 1) {
            return externalDirs[1];
        } else {
            return null;
        }
    }

    public static void saveEvent(final Context context, final int type, final String payload) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getAppDatabase(context);
                EventDao eventDao = db.eventDao();

                // TODO: check if RecordingSession is active and associate it with the event/error

                Event event = new Event();
                event.setTimestamp(Calendar.getInstance().getTime());
                event.setType(type);
                event.setPayload(payload);

                eventDao.insert(event);

            }
        });

    }


    public static void saveErrorEvent(Context context, String message, Throwable e) {
        saveErrorEvent(context, null, message, e);
    }

    public static void saveErrorEvent(Context context, Long sessionId, String message, Throwable e) {

        AppDatabase db = AppDatabase.getAppDatabase(context);
        ErrorEventDao errorEventDao = db.errorEventDao();

        ErrorEvent errorEvent = new ErrorEvent();

        errorEvent.setSessionId(sessionId); // TODO: try to obtain if null
        errorEvent.setTimestamp(Calendar.getInstance().getTime());
        errorEvent.setDescription(message);

        if (e != null) {
            errorEvent.setType(e.toString());
            errorEvent.setExceptionMessage(e.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorEvent.setStacktrace(sw.toString());
        }

        errorEventDao.insert(errorEvent);

        // FIXME: save ErrorEvent as Event too since I'm to lazy to add code for ErrorEvent syncing

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(" ");
        if (e != null) {
            sb.append(e.toString());
        }
        saveEvent(context, Event.EventType.ERROR, sb.toString());

    }

    public static Despat getDespat(Context context) {
        return ((Despat) context.getApplicationContext());
    }

    public static void redirectLogcat() {
        File appDirectory = Config.LOGCAT_DIR;
        File logDirectory = new File(appDirectory + "/log");

        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT_LOGFILE, new Locale("de", "DE"));
        File logFile = new File(logDirectory, "logcat_" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt");

        // create app folder
        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        // create log folder
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }

        if (logFile.exists()) {
            logFile.delete();
        }

        // clear the previous logcat and then write the new one to the file
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearLogcat() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void backupLogcat(String sessionName) {

        File appDirectory = Config.LOGCAT_DIR;
        File logDirectory = new File(appDirectory + "/log");
        File logFile = null;

        if (sessionName != null) {
            DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT_LOGFILE, new Locale("de", "DE"));
            logFile = new File(logDirectory, "logcat_" + sessionName + "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt");
        } else {
            DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT_LOGFILE, new Locale("de", "DE"));
            logFile = new File(logDirectory, "logcat_" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt");
        }

        // create app folder
        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        // create log folder
        if (!logDirectory.exists()) {
            logDirectory.mkdir();
        }

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");

            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("beginning of main")) continue;

                bufferedWriter.write(line);
                bufferedWriter.write("\n");
            }

        } catch (IOException e) {
            Log.w("logcatBackup", "IOException", e);
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (bufferedWriter != null) bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        clearLogcat();
    }

//    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
//        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
//        list.sort(Map.Entry.comparingByValue());
//
//        Map<K, V> result = new LinkedHashMap<>();
//        for (Map.Entry<K, V> entry : list) {
//            result.put(entry.getKey(), entry.getValue());
//        }
//
//        return result;
//    }

    public static Account createSyncAccount(Context context) {

        Account newAccount = new Account(Config.SYNC_ACCOUNT, Config.SYNC_ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.w(TAG, "account creation failed");
        }

        return newAccount;
    }

    public static void startSyncManually(Account syncAccount) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(syncAccount, Config.SYNC_AUTHORITY, settingsBundle);
        Log.i(TAG, "MANUAL SYNC REQUESTED");
    }

    public static void setHeartbeatManually(Context context, Class trigger) {
        Despat despat = Util.getDespat(context);
        AppDatabase db = AppDatabase.getAppDatabase(context);
        StatusDao statusDao = db.statusDao();
        Status lastStatus = statusDao.getLast();

        if (lastStatus != null) {
            long diff = Calendar.getInstance().getTime().getTime() - lastStatus.getTimestamp().getTime();

            if (diff < Config.getMinHeartbeatInterval(context)) {
                Log.d(TAG, "heartbeat triggered by [" +  trigger.getSimpleName() + "] aborted (min heartbeat interval)");
                return;
            }
        }

        Intent heartbeatIntent = new Intent(context, Orchestrator.class);
        heartbeatIntent.putExtra(Orchestrator.SERVICE, Broadcast.HEARTBEAT_SERVICE);
        heartbeatIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_ONCE);
        context.sendBroadcast(heartbeatIntent);

        Log.d(TAG, "manual Heartbeat trigger");
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // taken from https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    public static float distanceBetweenCoordinates(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private static double linearInterpolate(double a, double b, double f) {
        // return (a * (1.0 - f)) + (b * f);
        return a - ((a-b) * (1.0-f));
    }

    /* Calculates an exposure value
     *
     * exposureTime is expected to be ms
     *
     * Exposure with 1/4000 at f/22 and ISO 100 is equal to ~1.0 (extremly bright scene for AE)
     * Exposure with 1/2000 at f/16 and ISO 200 is equal to 3.0 (3 EV less light)
     */
    public static double computeExposureValue(long exposureTime, double aperture, int iso) {

        // Log.wtf(TAG, String.format("%f | %f | %d", exposureTime, aperture, iso));

        // convert exposureTime from ms to a floating point value of seconds (i.e. 0.5 --> half second)
        double exposureFraction = exposureTime/1000d;

        double shutter_repr     = Math.log(exposureFraction)/Math.log(2) + 13;
        double aperture_repr    = linearInterpolate(10, 1, (Math.log(aperture)/Math.log(2))/4.5d);
        double iso_repr         = Math.log(iso/100d)/Math.log(2) + 1;

        return shutter_repr + aperture_repr + iso_repr;
    }

    public static void shareFile(Context context, File f) throws Exception {

        if (f == null) {
            throw new Exception("File missing");
        }

        Uri fileUri = FileProvider.getUriForFile(context, "de.volzo.despat.fileprovider", f);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_STREAM, fileUri);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(i, "Share session data"));
    }

    /* Checks a list of possible OBB filenames
     * for a versionNumber of 3:
     * [main.3.de.volzo.despat.obb, main.2.de.volzo.despat.obb, ...]
     */
    public static String getObbPath(Context context) {
        int versionNumber = BuildConfig.VERSION_CODE;
        String version = Integer.toString(versionNumber);
        String packageName = context.getPackageName();

        String defaultObbName = context.getObbDir().getAbsolutePath() + "/" + "main." + version + "." + packageName + ".obb";

        for (File obbDir : context.getObbDirs()) {
            for (int i = versionNumber; i >= 1; i--) {
                String obbName = obbDir + "/" + "main." + i + "." + packageName + ".obb";

                File f = new File(obbName);
                if (!f.exists()) {
                    continue;
                }

                return obbName;
            }
        }

        return defaultObbName;
    }

    public static void loadObb(Context context) {
        // TODO
    }

    public static String getObbMountDir(Context context, StorageManager storageManager) {
        if (!storageManager.isObbMounted(getObbPath(context))) {
            return null;
        }

        return storageManager.getMountedObbPath(getObbPath(context));
    }

    public static void playSound(Context context) {

        final MediaPlayer mp = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);
        mp.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mp.release();
            }
        }, 1000);
    }

    public static void printCameraParameters(Context context) {
        try {
            Despat despat = getDespat(context);
            CameraController camera = despat.initCamera(context);
            HashMap<String, String> dict = camera.getCameraParameters();

            if (dict == null) throw new Exception("null parameters");

            for (Map.Entry<String, String> entry  : dict.entrySet()) {
                Log.i(TAG, entry.getKey() + " : " + entry.getValue());
            }
        } catch (Exception e) {
            Log.e(TAG, "printing camera parameters failed: ", e);
        }
    }

    public static void clearTextureView(TextureView tv) {
        Canvas canvas = tv.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "canvas unavailable");
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        tv.unlockCanvasAndPost(canvas);
    }

    public static void darkenTextureView(TextureView tv) {
        Canvas canvas = tv.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "canvas unavailable");
            return;
        }
        canvas.drawColor(Color.argb(255/2, 0, 0, 0));
        tv.unlockCanvasAndPost(canvas);
    }

    public static void drawTextOnTextureView(TextureView tv, String text) {
        Canvas canvas = tv.lockCanvas();
        if (canvas == null) {
            Log.e(TAG, "canvas unavailable");
            return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(canvas.getHeight());
        paint.setTypeface(Typeface.MONOSPACE);
        canvas.drawText(text, 0, 0, paint);
        tv.unlockCanvasAndPost(canvas);
    }

    public static void showSnackbar(Context context, String message, String reason) {
        Intent intent = new Intent(Broadcast.SHOW_MESSAGE);
        intent.putExtra(Broadcast.DATA_MESSAGE, message);
        intent.putExtra(Broadcast.DATA_REASON, reason);
        context.sendBroadcast(intent);
    }

    public static DateFormat getDateFormat() {
        return new SimpleDateFormat("MM.dd HH:mm:ss");
    }

    public static String getHumanReadableTimediff(Date d1, Date d2, boolean verbose) {
        long diff = d2.getTime() - d1.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();

        if (verbose) {
            if (diffDays > 0) {
                sb.append((int) diffDays);
                if (diffDays >= 2) {
                    sb.append(" days ");
                } else {
                    sb.append(" day ");
                }
            }

            if (diffHours > 0) {
                sb.append((int) diffHours);
                if (diffHours >= 2) {
                    sb.append(" hours ");
                } else {
                    sb.append(" hour ");
                }
            }

            if (diffMinutes > 0) {
                sb.append((int) diffMinutes);
                sb.append("min ");
            }

            if (diffSeconds > 0) {
                sb.append((int) diffSeconds);
                sb.append("s");
            }
        } else {
            if (diffDays > 0) {
                sb.append((int) diffDays);
                sb.append("d ");
            }

            sb.append((int) diffHours);
            sb.append(":");

            sb.append((int) diffMinutes);
            sb.append(":");

            sb.append((int) diffSeconds);
        }

        return sb.toString();
    }

    public static String getMostlyUniqueRandomString(Context context) {
        List<String> colors = new ArrayList<String>();
        List<String> animals = new ArrayList<String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("colors.txt"), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                //process line
                animals.add(line);
            }
        } catch (IOException e) {

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("animals.txt"), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                //process line
                animals.add(line);
            }
        } catch (IOException e) {

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        if (colors == null || animals == null || colors.size() < 10 || animals.size() < 10) {
            return UUID.randomUUID().toString();
        }

        int r1 = ThreadLocalRandom.current().nextInt(0, colors.size());
        int r2 = ThreadLocalRandom.current().nextInt(0, animals.size());

        return new String(colors.get(r1) + " " + animals.get(r2));
    }

    public static int[] listToPrimitiveInt(List<Integer> integers) throws NullPointerException {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    public static String listToString(List<String> arr) {
        StringBuilder sb = new StringBuilder();

        for (String item : arr) {
            sb.append(item);
            sb.append(","); // TODO
        }

        return sb.toString();
    }
    public static void cutList(List list, int maxSize) {
        int size = list.size();
        if (size > maxSize) {
            for (int i=0; i < (size-maxSize); i++) {
                list.remove(list.size()-1);
            }
        }
    }

    public static void deleteImage(File f) throws Exception {
        try {
            boolean success = f.getCanonicalFile().delete();
            if (!success) {
                throw new Exception("no success when deleting file");
            }
        } catch (IOException e) {
            Log.d(TAG, "problem deleting file", e);
            throw e;
        }
    }

    public static void deleteDirectory(File dir) throws Exception {
        if (!dir.exists()) {
            return;
        }

        if (!dir.isDirectory()) {
            throw new Exception("path is not a directory");
        }

        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            new File(dir, children[i]).delete();
        }

        dir.delete();
    }

    public static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    public static void copyAssets(Context context, String assetSrc, File dst) throws Exception {
        AssetManager assetManager = context.getAssets();

        if (assetSrc == null) {
            throw new NullPointerException();
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(assetSrc);
            out = new FileOutputStream(dst);

            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) > 0){
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + assetSrc, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    // ---

    // taken from the apache commons io library
    public static byte[] readFileToByteArray(final File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return toByteArray(in); // Do NOT use file.length() - see IO-453
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException ioe) {
                // ignore
            }
        }
    }

    private static byte[] toByteArray(final InputStream input) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static int copy(final InputStream input, final OutputStream output) throws IOException {
        int DEFAULT_BUFFER_SIZE = 1024 * 4;

        final long count = copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer) throws IOException {
        int EOF = -1;
        long count = 0;
        int n;

        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
