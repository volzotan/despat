package de.volzo.despat.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.volzo.despat.CameraController;
import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends IntentService {

    public static final String TAG = ShutterService.class.getName();

    private CameraController cameraController;

    public ShutterService() {
        super(ShutterService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //String dataString = workIntent.getDataString();

        // ...
        if (cameraController == null) {
            cameraController = new CameraController(this, new SurfaceTexture(0));
        }

        // pass Callback from this function to CameraController to get notified

        cameraController.takeImage();

        // notify
        Intent localIntent = new Intent(Broadcast.PICTURE_TAKEN).putExtra(Broadcast.DATA_PICTURE_PATH, "narf");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
