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
import de.volzo.despat.support.Broadcast;

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

        // check if any images needs to be deleted to have enough free space
        // may be time-consuming. alternative place to run?
//        ImageRollover imgroll = new ImageRollover(Config.IMAGE_FOLDER, Config.IMAGE_FILEEXTENSION);
//        imgroll.run();

        final Despat despat = ((Despat) getApplicationContext());

        despat.acquireWakeLock();
        CameraController camera = despat.getCamera();

        CameraController.ControllerCallback callback = new CameraController.ControllerCallback() {
            @Override
            public void cameraOpened() {

            }

            @Override
            public void cameraClosed() {

            }

            @Override
            public void cameraFailed() {

            }

            @Override
            public void intermediateImageTaken() {

            }

            @Override
            public void finalImageTaken() {

            }

            @Override
            public void captureComplete() {

                despat.closeCamera();
                despat.releaseWakeLock();

            }
        };

        try {
            if (camera == null || camera.getState() == CameraController.STATE_DEAD) {
                camera = new CameraController(this, null, null);
                despat.setCamera(camera);
            } else {
                camera.captureImages();
            }

        } catch (CameraAccessException cae) {
            Log.e(TAG, "taking photo failed", cae);
            despat.releaseWakeLock();
            // throw cae;
        } catch (Exception e) {
            Log.e(TAG, "taking photo failed", e);
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
