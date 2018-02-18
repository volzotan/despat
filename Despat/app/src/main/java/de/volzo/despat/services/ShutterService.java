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

import java.io.File;
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

    Runnable cameraKillRunnable = new Runnable() {
        @Override
        public void run() {
            Log.wtf(TAG, "CAMERA WATCHDOG KILL");
            Util.saveEvent(context, Event.EventType.ERROR, "camera: watchdog kill");

            Despat despat = Util.getDespat(context);
            despat.closeCamera();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        this.context = this;
        this.handler = new Handler();

        startForeground(FOREGROUND_NOTIFICATION_ID, Util.getNotification(context, 0));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        registerReceiver(shutterReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        registerReceiver(powerReceiver, filter);

        // start and release shutter
        releaseShutter();

        return START_STICKY;
    }

    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            Util.saveEvent(context, Event.EventType.SLEEP_MODE_CHANGE, "in idle mode: " + pm.isDeviceIdleMode());
            Log.d(TAG, "+++ in idle mode: " + pm.isDeviceIdleMode());
        }
    };

    private final BroadcastReceiver shutterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            releaseShutter();
        }
    };

    public void releaseShutter() {

        final Despat despat = Util.getDespat(this);
        SystemController systemController = despat.getSystemController();

        despat.acquireWakeLock();
        handler.postDelayed(cameraKillRunnable, Config.SHUTTER_CAMERA_MAX_LIFETIME);

        RecordingSession session = RecordingSession.getInstance(this);
        Log.i(TAG, "shutter released. BATT: " + systemController.getBatteryLevel() + "% | IMAGES: " + session.getImagesTaken());

        // check if any images needs to be deleted to have enough free space
        // may be time-consuming. alternative place to run?
        ImageRollover imgroll = new ImageRollover(despat, null);
        imgroll.run();

        CameraController camera = despat.getCamera();

        CameraController.ControllerCallback callback = new CameraController.ControllerCallback() {
            @Override
            public void captureComplete() {
                despat.closeCamera();
            }

            @Override
            public void cameraClosed() {
                shutterReleaseFinished();
            }

            @Override
            public void cameraFailed(String message, Object o) {
                Log.e(TAG, "camera failed");

                String err = "";
                if (o != null) {
                    try {
                        err = o.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append("camera failed");
                if (message != null) {
                    sb.append(": ");
                    sb.append(message);
                }
                if (err != null && !err.isEmpty()) {
                    sb.append(" [");
                    sb.append(err);
                    sb.append("]");
                }

                Util.saveEvent(despat, Event.EventType.ERROR, sb.toString());

                // camera should close itself and call onClosed
            }
        };

        try {
            if (camera == null || camera.isDead()) {
                Log.d(TAG, "CamController created");
                camera = despat.initCamera(this, callback, null);
                camera.openCamera();
            } else {
                Log.d(TAG, "CamController already up and running");
                camera.captureImages();
            }
        } catch (Exception e) {
            // opening camera failed

            Log.e(TAG, "taking photo failed (error during opening camera)", e);
            Util.saveEvent(this, Event.EventType.ERROR, "shutter failed: " + e.getMessage());

            // critical error
            if (Config.REBOOT_ON_CRITICAL_ERROR) despat.criticalErrorReboot();

            shutterReleaseFinished();
        }
    }

    private void shutterReleaseFinished() {
        handler.removeCallbacks(cameraKillRunnable);

        Despat despat = Util.getDespat(this);
        despat.releaseWakeLock();
    }

    @Override
    public void onDestroy() {
        Despat despat = ((Despat) getApplicationContext());

        // is the camera still alive?
        if (despat.getCamera() != null && !despat.getCamera().isDead()) {
            Log.e(TAG, "camera still alive on ShutterService destroy");
            Util.saveEvent(despat, Event.EventType.ERROR, "camera still alive on ShutterService destroy");
        }

        unregisterReceiver(shutterReceiver);
        unregisterReceiver(powerReceiver);
        handler.removeCallbacksAndMessages(null);
        despat.releaseWakeLock();
        stopForeground(true);

        Log.d(TAG, "shutterService destroyed");
    }

}
