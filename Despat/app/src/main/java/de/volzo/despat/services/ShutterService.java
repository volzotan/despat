package de.volzo.despat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.ImageRollover;
import de.volzo.despat.RecordingSession;
import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends Service {

    /**
     * A word of caution:
     *
     * running the ShutterService in its own process
     * ( via defining android:process=":despatShutterService" in AndroidManifest.xml" )
     * will yield inconsistent states when accessing sharedPreferences. The process obtains
     * a memory cached copy at startup and ignores any further changes. The app needs to be
     * restarted to update the values the process accesses.
     *
     * (SharedPref docs: "Note: This class does not support use across multiple processes." )
     *
     */

    public static final String TAG = ShutterService.class.getSimpleName();
    public static final int REQUEST_CODE                = 0x1200;
    public static final int FOREGROUND_NOTIFICATION_ID  = 0x0500;

    private static final int STATE_INIT                 = 0;
    private static final int STATE_CAMERA_READY         = 1;
    private static final int STATE_BUSY                 = 2;
    private static final int STATE_ERROR                = 3;
    private static final int STATE_RESTART              = 4;
    private static final int STATE_SHUTDOWN             = 5;

    private static final long MIN_TIME_BETWEEN_CAMERA_RESTARTS = 7 * 1000;

    Context context;
    Handler handler;

    CameraController camera;

    private int state = STATE_INIT;
    private long lastCameraRestart;

    public ShutterService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // ---------------------------------------------------------------------------------------------

    Runnable cameraWatchdogRunnable = new Runnable() {
        @Override
        public void run() {
            Log.wtf(TAG, "CAMERA WATCHDOG KILL");
            Util.saveEvent(context, Event.EventType.ERROR, "camera: watchdog kill");

            if (Config.getPersistentCamera(context)) {
                // TODO: reschedule new shutterRelease? restart camera?
            } else {
                Despat despat = Util.getDespat(context);
                despat.closeCamera();
            }
        }
    };

    Runnable shutterReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            Intent triggerIntent = new Intent();
            triggerIntent.setAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
            context.sendBroadcast(triggerIntent);
        }
    };

    // ---------------------------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.wtf(TAG, "ShutterService is running on UI thread");
        } else {
            Log.d(TAG, "ShutterService is running on thread: " + Thread.currentThread().getName());
        }

        Log.d(TAG, "SHUTTER SERVICE invoked");

        this.context = this;
        this.handler = new Handler();

        startForeground(FOREGROUND_NOTIFICATION_ID, Util.getShutterNotification(context, 0, 0, null));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        registerReceiver(shutterReceiver, filter);

        // TODO: receiving these broadcasts as the shutterService can lead to critical app crashes
//        filter = new IntentFilter();
//        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
//        filter.addAction("android.intent.action.SCREEN_OFF");
//        filter.addAction("android.intent.action.SCREEN_ON");
//        registerReceiver(powerReceiver, filter);

        Despat despat = Util.getDespat(this);

        if (Config.getPersistentCamera(context)) {
            despat.acquireWakeLock(false);

            try {
                startCamera();
            } catch (Exception e) {
                state = STATE_ERROR;
                eventMalfunction("error during opening camera", e);

                // if the service fails to start correctly
                // it should not be restarted automatically

                return START_NOT_STICKY;
            }
        } else {
            handler.post(shutterReleaseRunnable);
        }

        return START_STICKY;
    }

    // ---------------------------------------------------------------------------------------------

    private void startCamera() throws Exception {

        final Despat despat = Util.getDespat(this);

        // check if any images needs to be deleted to have enough free space
        // TODO: may be time-consuming. alternative place to run?
        ImageRollover imgroll = new ImageRollover(despat, null);
        imgroll.run();

        camera = despat.getCamera();

        try {
            if (camera == null || camera.isDead()) {
                Log.d(TAG, "CamController created");
                camera = despat.initCamera(this, cameraCallback, null);
                camera.openCamera();
            } else {
                Log.d(TAG, "CamController already up and running");
                eventCameraReady();
            }

        } catch (Exception e) {
            // opening camera failed
            // calling entity needs to decide
            // if a restart may be necessary
            throw e;
        }
    }

    private void restartCamera() {
        Log.i(TAG, "camera restart");
        state = STATE_RESTART;
        shutdownCamera();
    }

    private void restartCameraAfterMalfunction() {
        Log.w(TAG, "camera restart after malfunction");
        long diff = System.currentTimeMillis() - lastCameraRestart;

        if (lastCameraRestart == 0 || diff > MIN_TIME_BETWEEN_CAMERA_RESTARTS) {
            lastCameraRestart = System.currentTimeMillis();

            Util.saveEvent(this, Event.EventType.ERROR, "camera restart after malfunction");
            restartCamera();

        } else {
            Log.w(TAG, String.format("camera restart after malfunction aborted. last restart happened %dms ago", diff));

            // TODO: critical error?

            // do nothing
            state = STATE_ERROR;
            abortAfterMalfunction();
            return;
        }
    }

    private void triggerCamera() {

        if (Config.RERUN_METERING_BEFORE_CAPTURE) {
            try {
                camera.startMetering();
                state = STATE_BUSY;
            } catch (Exception e) {
                eventMalfunction("starting metering failed", e);
                restartCameraAfterMalfunction();
            }
        } else {
            try {
                camera.captureImages();
                state = STATE_BUSY;
            } catch (Exception e) {
                eventMalfunction("capturing image failed", e);
            }
        }
    }

    private void shutdownCamera() {
        Despat despat = Util.getDespat(context);
        despat.closeCamera();
    }

    private void shutdownService() {

    }

    private void abortAfterMalfunction() {
        Log.e(TAG, "abort after malfunction");

        ArrayList<String> addInfo = new ArrayList<String>();
        addInfo.add("ShutterService aborted after restarting failed");
        Util.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, 0, 1, addInfo);

        if (Config.getPersistentCamera(context)) {
            handler.removeCallbacksAndMessages(null);
        } else {
            // TODO
        }

        Util.saveEvent(this, Event.EventType.ERROR, "abort after malfunction");
    }

    // ---------------------------------------------------------------------------------------------

    private void eventCameraReady() {

        if (Config.getPersistentCamera(context)) {

            switch (state) {
                case STATE_INIT: {
                    state = STATE_CAMERA_READY;
                    handler.post(shutterReleaseRunnable);
                    break;
                }
                case STATE_RESTART: {
                    // do not schedule a new trigger event, trigger right away
                    triggerCamera();
                    break;
                }
                default: {
                    eventMalfunction("invalid state in eventCameraReady: " + state);
                }
            }

        } else {
            triggerCamera();
        }
    }

    private void eventCaptureComplete() {
        state = STATE_CAMERA_READY;

        if (!Config.getPersistentCamera(context)) {
            shutdownCamera();
        }
    }


    private void eventMalfunction(String message, Object o) {

        // Something dire happened

        // create error message
        StringBuilder sb = new StringBuilder();
        sb.append("ShutterService failed: ");
        sb.append(message);

        Throwable e = null;

        if (o != null) {
            sb.append(" | ");

            if (o instanceof Throwable) {
                e = (Throwable) o;
                sb.append(e.getMessage());
                sb.append(" --- ");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                sb.append(sw.toString());
            } else {
                sb.append(o.toString());
            }
        }

        Log.e(TAG, sb.toString());

        // persist error
        Util.saveEvent(this, Event.EventType.ERROR, sb.toString());

        // send error broadcast
        Intent intent = new Intent(Broadcast.ERROR_OCCURED);
        // TODO pack throwable in parcel
        if (e != null) intent.putExtra(Broadcast.DATA_THROWABLE, e);
        context.sendBroadcast(intent);

        // backup logcat
        Util.backupLogcat(null);

        // notify user
        Toast.makeText(context, "ShutterService failed: " + message, Toast.LENGTH_SHORT).show();

        ArrayList<String> addInfo = new ArrayList<String>();
        addInfo.add("ShutterService crashed");
        Util.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, 0, 1, addInfo);
    }

    private void eventMalfunction(String message) {
        eventMalfunction(message, null);
    }

    private void eventCameraShutdown() {
        if (Config.getPersistentCamera(context)) {

            switch (state) {
                case STATE_INIT: {
                    // camera failed during startup. do nothing
                    state = STATE_ERROR;
                    break;
                }
                case STATE_RESTART: {
                    try {
                        startCamera();
                    } catch (Exception e) {
                        eventMalfunction("restarting failed", e);
                        state = STATE_ERROR;
                        // do nothing
                    }
                    break;
                }
                case STATE_BUSY: {
                    // error occured while taking image
                    restartCameraAfterMalfunction();
                    return;
                }
                case STATE_SHUTDOWN: {
                    // onDestroy handles everything. do nothing
                    break;
                }
                case STATE_CAMERA_READY: {
                    eventMalfunction("something went wrong, camera was closed unexpectedly");
                    state = STATE_ERROR;
                    // do nothing
                    break;
                }
                case STATE_ERROR: {
                    // double error. do nothing
                    break;
                }
                default: {
                    eventMalfunction("invalid state in eventCameraShutdown: " + state);
                }
            }
        } else {
            handler.removeCallbacks(cameraWatchdogRunnable);
            Despat despat = Util.getDespat(this);
            despat.releaseWakeLock();
        }
    }


    private CameraController.ControllerCallback cameraCallback = new CameraController.ControllerCallback() {
        @Override
        public void cameraOpened() {
            Log.d(TAG, ":: cameraOpened");
        }

        @Override
        public void cameraReady(CameraController camera) {
            Log.d(TAG, ":: cameraReady");
            eventCameraReady();
        }

        @Override
        public void cameraFocused(CameraController camera, boolean afSuccessful) {
            Log.d(TAG, ":: cameraFocused (" + afSuccessful + ")");

            try {
                camera.captureImages();
            } catch (Exception e) {
                eventMalfunction("capturing images failed", e);
            }
        }

        @Override
        public void intermediateImageTaken() {
            Log.d(TAG, ":: intermediateImageTaken");
        }

        @Override
        public void captureComplete() {
            Log.d(TAG, ":: captureComplete");

            eventCaptureComplete();
        }

        @Override
        public void cameraClosed() {
            Log.d(TAG, ":: cameraClosed");
            eventCameraShutdown();
        }

        @Override
        public void cameraFailed(String message, Object o) {
            eventMalfunction("camera failed: + " + message, o);

            // camera should close itself and call onClosed
        }
    };

    // ---------------------------------------------------------------------------------------------

    private final BroadcastReceiver shutterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Despat despat = Util.getDespat(context);
            SystemController systemController = despat.getSystemController();
            RecordingSession session = RecordingSession.getInstance(context);
            Log.i(TAG, "shutter released. BATT: " + systemController.getBatteryLevel() + "% | IMAGES: " + session.getImagesTaken());

            if (Config.getPersistentCamera(context)) {

                long now = System.currentTimeMillis();
                long nextExecution = now + Config.getShutterInterval(context);
                nextExecution -= nextExecution % 1000;
                long delay = nextExecution - now;
                Log.d(TAG, "delay: " + delay);
                handler.postDelayed(shutterReleaseRunnable, delay);

                Config.setNextShutterServiceInvocation(context, nextExecution);

                triggerCamera();

            } else {

                despat.acquireWakeLock(true);
                handler.postDelayed(cameraWatchdogRunnable, Config.SHUTTER_CAMERA_MAX_LIFETIME);

                try {
                    startCamera();
                } catch (Exception e) {
                    eventMalfunction("error during opening camera", e);

                    shutdownCamera();
                }
            }
        }
    };

    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED:
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    Util.saveEvent(context, Event.EventType.SLEEP_MODE_CHANGE, "in idle mode: " + pm.isDeviceIdleMode());
                    Log.d(TAG, "+++ in idle mode: " + pm.isDeviceIdleMode());
                    break;

                case "android.intent.action.SCREEN_OFF":
                    Util.saveEvent(context, Event.EventType.DISPLAY_OFF, null);
                    Log.d(TAG, "+++ screen off");
                    break;

                case "android.intent.action.SCREEN_ON":
                    Util.saveEvent(context, Event.EventType.DISPLAY_ON, null);
                    Log.d(TAG, "+++ screen on");
                    break;

                default:
                    Log.e(TAG, "unknown intent: " + intent);
            }
        }
    };

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        state = STATE_SHUTDOWN;
        Despat despat = ((Despat) getApplicationContext());

        if (Config.getPersistentCamera(context)) {

            if (despat.getCamera() != null && !despat.getCamera().isDead()) {
                camera.closeCamera();
            } else {
                // camera already dead. something went wrong
            }

        } else {
            // is the camera still alive?
            if (despat.getCamera() != null && !despat.getCamera().isDead()) {
                Log.e(TAG, "camera still alive on ShutterService destroy");
                Util.saveEvent(despat, Event.EventType.ERROR, "camera still alive on ShutterService destroy");

                camera.closeCamera();
            }
        }

        try {
            unregisterReceiver(shutterReceiver);
        } catch (RuntimeException e) {
            Log.w(TAG, "unable to unregister receiver");
        }

        try {
            unregisterReceiver(powerReceiver);
        } catch (RuntimeException e) {
            Log.w(TAG, "unable to unregister receiver");
        }

        handler.removeCallbacksAndMessages(null);
        despat.releaseWakeLock();
        stopForeground(true);

        Log.d(TAG, "shutterService destroyed");
    }

}
