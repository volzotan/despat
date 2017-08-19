package de.volzo.despat.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.volzo.despat.CameraController;
import de.volzo.despat.Orchestrator;
import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends IntentService {

    public static final String TAG = ShutterService.class.getName();
    public static final int REQUEST_CODE = 1234;

    private CameraController cameraController2;

    public ShutterService() {
        super(ShutterService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "SHUTTER SERVICE invoked");

        //String dataString = workIntent.getDataString();

        // ...
//        if (cameraController == null) {
//            cameraController = new CameraController(this, new SurfaceTexture(0));
//        }
//
//        // pass Callback from this function to CameraController to get notified
//
//        cameraController.takeImage();

        // notify
        Intent localIntent = new Intent(Broadcast.PICTURE_TAKEN).putExtra(Broadcast.DATA_PICTURE_PATH, "narf");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public static boolean isRunning(Context context) {

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        // TODO: not sure if really needed:
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);

        boolean alarmUp = (PendingIntent.getBroadcast(context, REQUEST_CODE,
                shutterIntent, PendingIntent.FLAG_NO_CREATE) != null);

        return alarmUp;
    }
}
