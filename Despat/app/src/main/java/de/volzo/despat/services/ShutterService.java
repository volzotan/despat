package de.volzo.despat.services;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.volzo.despat.CameraController;
import de.volzo.despat.MainActivity;
import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.08.17.
 */

public class ShutterService extends JobService {

    public static final String TAG = ShutterService.class.getName();

    private CameraController cameraController;

    public boolean onStartJob(JobParameters jobParameters) {
        //String dataString = workIntent.getDataString();

        // ...
        if (cameraController == null) {
            cameraController = new CameraController(this, new SurfaceTexture(0));
        }
        cameraController.takeImage();

        // notify
        Intent localIntent = new Intent(Broadcast.PICTURE_TAKEN).putExtra(Broadcast.DATA_PICTURE_PATH, "narf");
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        return true;
    }

    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "job stopped");

        return true;
    }

}
