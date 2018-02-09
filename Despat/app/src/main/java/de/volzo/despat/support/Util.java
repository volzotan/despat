package de.volzo.despat.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.MainActivity;
import de.volzo.despat.R;
import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.EventDao;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.services.ShutterService;

import static android.content.Context.ACCOUNT_SERVICE;

/**
 * Created by volzotan on 16.08.17.
 */

public class Util {

    public static final String TAG = Util.class.getSimpleName();

    public static Notification getNotification(Context context, int numberOfCaptures) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(context.getApplicationContext())
                .setContentTitle("despat active")
                .setContentText(numberOfCaptures + " captures")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        return notification;
    }

    public static void updateNotification(Context context, int notificationIdentifier, int numberOfCaptures) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification not : notifications) {
            if (not.getId() == notificationIdentifier) {
                // only update if already existing
                notificationManager.notify(notificationIdentifier, getNotification(context, numberOfCaptures));
                return;
            }
        }

        Log.w(TAG, "no notification to update");
    }

    public static void stopNotification(Context context, int notificationIdentifier) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationIdentifier);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static float getFreeSpaceOnDevice(File dir) {
        StatFs stat = new StatFs(dir.getPath());
        long bytesAvailable = 0;
        bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();

        return bytesAvailable / (1024.f * 1024.f);
    }

    public static void saveEvent(Context context, int type, String payload) {

        AppDatabase db = AppDatabase.getAppDatabase(context);
        EventDao eventDao = db.eventDao();

        Event event = new Event();
        event.setTimestamp(Calendar.getInstance().getTime());
        event.setType(type);
        event.setPayload(payload);

        eventDao.insert(event);
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
            logFile = new File(logDirectory, "logcat_" + sessionName + ".txt");
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

    public static void purgeDatabase(Context context) {
        AppDatabase db = AppDatabase.getAppDatabase(context);

        db.eventDao().dropTable();
        db.captureDao().dropTable();
        db.sessionDao().dropTable();
        db.statusDao().dropTable();
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableDoze() {
//        try {
//            Process process = Runtime.getRuntime().exec("adb shell dumpsys deviceidle disable");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        BufferedReader bufferedReader = null;

        try {
            Process process = Runtime.getRuntime().exec("dumpsys deviceidle disable");

            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            Log.w("print", "IOException", e);
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            despat.initCamera();
            CameraController cameraController = despat.getCamera();
            HashMap<String, String> dict = cameraController.getCameraParameters();

            if (dict == null) throw new Exception("null parameters");

            for (Map.Entry<String, String> entry  : dict.entrySet()) {
                Log.i(TAG, entry.getKey() + " : " + entry.getValue());
            }
        } catch (Exception e) {
            Log.e(TAG, "printing camera parameters failed: ", e);
        }
    }

    public static String getHumanReadableTimediff(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();

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
//            if (diffMinutes >= 2) {
//                sb.append(" minutes ");
//            } else {
//                sb.append(" minute ");
//            }
        }

        if (diffSeconds > 0) {
            sb.append((int) diffSeconds);
            sb.append("s");
//            if (diffSeconds >= 2) {
//                sb.append(" seconds");
//            } else {
//                sb.append(" second");
//            }
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
