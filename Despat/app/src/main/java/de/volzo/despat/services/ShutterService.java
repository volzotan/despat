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
import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

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

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends Service {

    public static final String TAG = ShutterService.class.getSimpleName();
    public static final int REQUEST_CODE                = 0x1200;
    public static final int FOREGROUND_NOTIFICATION_ID  = 0x0500;

    Timer timer;

    public ShutterService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        final Context context = this;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context.getApplicationContext())
                .setContentTitle("despat Shutter Service")
                .setContentText("active")
                .setSmallIcon(R.drawable.ic_notification)
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setTicker("ticker text")
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        registerReceiver(broadcastReceiver, filter);

        // start and release shutter
        releaseShutter();

        // watchdog: Service must be dead 5 seconds after start
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.wtf(TAG, "SHUTTER SERVICE WATCHDOG KILL");
                Util.saveEvent(context, Event.EventType.ERROR, "ShutterService: watchdog kill");

                Intent shutterServiceIntent = new Intent(context, ShutterService.class);
                context.stopService(shutterServiceIntent);
            }
        }, 5000);

        return START_NOT_STICKY;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            releaseShutter();
        }
    };

    public void releaseShutter() {

        final Despat despat = Util.getDespat(this);
        SystemController systemController = despat.getSystemController();

        despat.acquireWakeLock();

        RecordingSession session = RecordingSession.getInstance(this);
        Log.i(TAG, "shutter released. BATT: " + systemController.getBatteryLevel() + "% | IMAGES: " + session.getImagesTaken());

        // check if any images needs to be deleted to have enough free space
        // may be time-consuming. alternative place to run?
        ImageRollover imgroll = new ImageRollover(despat);
        imgroll.run();

        CameraController camera = despat.getCamera();

        CameraController.ControllerCallback callback = new CameraController.ControllerCallback() {
            @Override
            public void captureComplete() {
                despat.closeCamera();
            }

            @Override
            public void cameraClosed() {

                // Nexus 5: nasty camera-close-bug workaround
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent shutterServiceIntent = new Intent(despat, ShutterService.class);
                        stopService(shutterServiceIntent);
                    }
                }, 500);
            }
        };

        try {
            if (camera == null || camera.isDead()) {
                Log.d(TAG, "CamController created");
                camera = new CameraController(this, callback, null);
                despat.setCamera(camera);
            } else {
                Log.d(TAG, "CamController already up and running");
                camera.captureImages();
            }
        } catch (Exception e) {
            Log.e(TAG, "taking photo failed", e);

            Util.saveEvent(this, Event.EventType.ERROR, "shutter failed: " + e.getMessage());

            // critical error
            despat.criticalErrorReboot();

            despat.releaseWakeLock();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        timer.cancel();

        Despat despat = ((Despat) getApplicationContext());
        despat.closeCamera();// TODO: close camera?
        despat.releaseWakeLock();

        stopForeground(true);

        Log.d(TAG, "shutterService destroyed");
    }

}
