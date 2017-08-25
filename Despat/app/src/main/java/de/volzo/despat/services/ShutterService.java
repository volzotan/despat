package de.volzo.despat.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.volzo.despat.CameraController;
import de.volzo.despat.CameraController2;
import de.volzo.despat.Despat;
import de.volzo.despat.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.CameraAdapter;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends IntentService {

    public static final String TAG = ShutterService.class.getName();
    public static final int REQUEST_CODE = 0x1200;

    public ShutterService() {
        super(ShutterService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        // TODO: acquire Wake Lock?

        final Context context = this;

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Despat despat = ((Despat) getApplicationContext());
                CameraAdapter camera = despat.getCamera();
                
                try {

                    if (camera == null || camera.getState() == CameraAdapter.STATE_DEAD) {
                        camera = new CameraController2(context, null, CameraController2.OPEN_AND_TAKE_PHOTO);
                        despat.setCamera(camera);
                    } else {
                        camera.takePhoto();
                    }

                } catch (CameraAccessException cae) {
                    Log.e(TAG, "taking photo failed", cae);
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "taking photo failed", e);
                    return;
                }
            }
        });
    }

    public static boolean isRunning(Context context) {

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        // TODO: not sure if really needed:
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);

        boolean alarmUp = (PendingIntent.getBroadcast(context, REQUEST_CODE, shutterIntent, PendingIntent.FLAG_NO_CREATE) != null);

        return alarmUp;
    }
}
