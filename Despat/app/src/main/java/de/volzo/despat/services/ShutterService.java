package de.volzo.despat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.os.IBinder;
import android.util.Log;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.ImageRollover;
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
    public static final int REQUEST_CODE = 0x1200;

    public ShutterService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        registerReceiver(broadcastReceiver, filter);

        // start and release shutter
        releaseShutter();

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
                // despat.releaseWakeLock();

                Intent shutterServiceIntent = new Intent(despat, ShutterService.class);
                stopService(shutterServiceIntent);
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
            despat.releaseWakeLock();
            // throw e;
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        Despat despat = ((Despat) getApplicationContext());
        despat.releaseWakeLock();

        // TODO: close camera?

        Log.d(TAG, "shutterService destroyed");
    }

//    public static boolean isRunning(Context context) {
//
//        Intent shutterIntent = new Intent(context, Orchestrator.class);
//        // TODO: not sure if really needed:
//        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
//        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
//
//        boolean alarmUp = (PendingIntent.getBroadcast(context, REQUEST_CODE, shutterIntent, PendingIntent.FLAG_NO_CREATE) != null);
//
//        return alarmUp;
//    }
}
