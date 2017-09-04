package de.volzo.despat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.os.IBinder;
import android.util.Log;

import de.volzo.despat.CameraController2;
import de.volzo.despat.Despat;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.CameraAdapter;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends Service {

    public static final String TAG = ShutterService.class.getName();
    public static final int REQUEST_CODE = 0x1200;


    public ShutterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        // TODO: acquire Wake Lock?

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
        Despat despat = ((Despat) getApplicationContext());
        CameraAdapter camera = despat.getCamera();

        try {
            if (camera == null || camera.getState() == CameraAdapter.STATE_DEAD) {
                camera = new CameraController2(this, null, CameraController2.OPEN_AND_TAKE_PHOTO);
                despat.setCamera(camera);
            } else {
                camera.takePhoto();
            }

        } catch (CameraAccessException cae) {
            Log.e(TAG, "taking photo failed", cae);
            // throw cae;
        } catch (Exception e) {
            Log.e(TAG, "taking photo failed", e);
            // throw e;
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

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
