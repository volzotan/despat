package de.volzo.despat.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.hardware.camera2.CameraAccessException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.ImageRollover;
import de.volzo.despat.MainActivity;
import de.volzo.despat.R;
import de.volzo.despat.RecordingSession;
import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.Sync;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends Service {

    public static final String TAG = ShutterService.class.getSimpleName();
    public static final int REQUEST_CODE                = 0x1200;
    public static final int FOREGROUND_NOTIFICATION_ID  = 0x0500;

    Context context;
    Handler handler;

    CameraController camera;

    private boolean shutdownMode = false;

    public ShutterService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*

    onStartCommand
        timer started
        releaseShutter -->

    releaseShutter / on error --> killServiceDelayed --> onDestroy
        callback --> complete --> closed
                     closed   --> killServiceDelayed --> onDestroy
                     failed   --> closed

    timer --> onDestroy

    onDestroy releaseWakelock|stopForeground


    */

    Runnable cameraWatchdogRunnable = new Runnable() {
        @Override
        public void run() {
            Log.wtf(TAG, "CAMERA WATCHDOG KILL");
            Util.saveEvent(context, Event.EventType.ERROR, "camera: watchdog kill");

            if (Config.PERSISTENT_CAMERA) {
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

        // TODO: somehow receiving these broadcasts as the shutterService can lead to critical app crashes

//        filter = new IntentFilter();
//        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
//        filter.addAction("android.intent.action.SCREEN_OFF");
//        filter.addAction("android.intent.action.SCREEN_ON");
//        registerReceiver(powerReceiver, filter);

        Despat despat = Util.getDespat(this);

        if (Config.PERSISTENT_CAMERA) {
            despat.acquireWakeLock(false);
            handler.postDelayed(shutterReleaseRunnable, 5000);

            // start and release shutter
            try {
                initCamera();

            } catch (Exception e) {
                logError("error during opening camera", e);

                shutterReleaseFinishedAndCameraClosed(); // TODO: is it really clever to use this here?

                // critical error
                if (Config.REBOOT_ON_CRITICAL_ERROR) {
                    despat.criticalErrorReboot();
                } else {
                    // if the service fails to start correctly
                    // it should not be restarted automatically

                    return START_NOT_STICKY;
                }
            }
        }

        return START_STICKY;
    }

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

    private final BroadcastReceiver shutterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Despat despat = Util.getDespat(context);
            SystemController systemController = despat.getSystemController();
            RecordingSession session = RecordingSession.getInstance(context);
            Log.i(TAG, "shutter released. BATT: " + systemController.getBatteryLevel() + "% | IMAGES: " + session.getImagesTaken());

            if (Config.PERSISTENT_CAMERA) {

                long now = System.currentTimeMillis();
                long nextExecution = now + Config.getShutterInterval(context);
                nextExecution -= nextExecution % 1000;
                long delay = nextExecution - now;
                Log.d(TAG, "delay: " + delay);
                handler.postDelayed(shutterReleaseRunnable, delay);

                Config.setNextShutterServiceInvocation(context, nextExecution);

                if (Config.RERUN_METERING_BEFORE_CAPTURE) {
                    try {
                        camera.startMetering();
                    } catch (Exception e) {
                        logError("starting metering failed", e);
                    }
                } else {
                    try {
                        camera.captureImages();
                    } catch (Exception e) {
                        logError("capturing image failed", e);
                    }
                }

            } else {

                despat.acquireWakeLock(true);
                handler.postDelayed(cameraWatchdogRunnable, Config.SHUTTER_CAMERA_MAX_LIFETIME);

                try {
                    initCamera();
                } catch (Exception e) {
                    logError("error during opening camera", e);

                    shutterReleaseFinishedAndCameraClosed();

                    // critical error
                    if (Config.REBOOT_ON_CRITICAL_ERROR) {
                        despat.criticalErrorReboot();
                    }
                }
            }
        }
    };

    private CameraController.ControllerCallback cameraCallback = new CameraController.ControllerCallback() {
        @Override
        public void cameraOpened() {
            Log.d(TAG, ":: cameraOpened");
        }

        @Override
        public void cameraReady(CameraController camera) {
            Log.d(TAG, ":: cameraReady");

            try {
                camera.startMetering();
            } catch (Exception e) {
                logError("metering failed: + ", e);
            }
        }

        @Override
        public void cameraFocused(CameraController camera, boolean afSuccessful) {
            Log.d(TAG, ":: cameraFocused (" + afSuccessful + ")");

            try {
                camera.captureImages();
            } catch (Exception e) {
                logError("capturing images failed", e);
            }
        }

        @Override
        public void intermediateImageTaken() {
            Log.d(TAG, ":: intermediateImageTaken");
        }

        @Override
        public void captureComplete() {
            Log.d(TAG, ":: captureComplete");

            if (!Config.PERSISTENT_CAMERA) {
                Despat despat = Util.getDespat(context);
                despat.closeCamera();
            }
        }

        @Override
        public void cameraClosed() {
            Log.d(TAG, ":: cameraClosed");
            shutterReleaseFinishedAndCameraClosed();
        }

        @Override
        public void cameraFailed(String message, Object o) {
            logError("camera failed: + " + message, o);

            // camera should close itself and call onClosed
        }
    };

    private void initCamera() throws Exception {

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
                camera.captureImages();
            }

        } catch (Exception e) { // opening camera failed
            throw e;
        }
    }

//    public void scheduleShutterRelease() {
//        handler.removeCallbacks(cameraWatchdogRunnable);
//
//        long now = System.currentTimeMillis();
//        long nextExecution = now + Config.getShutterInterval(context);
//        nextExecution -= nextExecution % 1000;
//        long delay = nextExecution - now;
//        Log.d(TAG, "delay: " + delay);
//        handler.postDelayed(cameraReleaseRunnable, delay);
//        handler.postDelayed(cameraWatchdogRunnable, delay + 1000);
//    }


    private void shutterReleaseFinishedAndCameraClosed() {
        if (Config.PERSISTENT_CAMERA) {
            if (shutdownMode) {
                // pass
            } else {
                Log.e(TAG, "something went wrong, camera was closed unexpectedly");

                // TODO: if camera restart is attempted, loop must be avoided
                // initCamera();

                ArrayList<String> addInfo = new ArrayList<String>();
                addInfo.add("ShutterService crashed");
                Util.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, 0, 1, addInfo);
            }
        } else {
            handler.removeCallbacks(cameraWatchdogRunnable);
            Despat despat = Util.getDespat(this);
            despat.releaseWakeLock();
        }
    }

    private void logError(String message, Object o) {
        StringBuilder sb = new StringBuilder();
        sb.append("ShutterService failed: ");
        sb.append(message);
        sb.append(" | ");

        sb.append(o.toString());

        Log.e(TAG, sb.toString());
        Util.saveEvent(this, Event.EventType.ERROR, sb.toString());

        Toast.makeText(context, "ShutterService failed: " + message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String message, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("ShutterService failed: ");
        sb.append(message);
        sb.append(" | ");
        sb.append(e.getMessage());
        sb.append("\n --- \n");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        sb.append(sw.toString());

        logError(message, sb);
    }

    @Override
    public void onDestroy() {
        Despat despat = ((Despat) getApplicationContext());

        if (Config.PERSISTENT_CAMERA) {

            if (despat.getCamera() != null && !despat.getCamera().isDead()) {
                this.shutdownMode = true;
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
