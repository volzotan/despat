package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import java.io.File;

import de.volzo.despat.Despat;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.SystemController;
import de.volzo.despat.web.ServerConnector;


public class UploadService extends JobService {

    private static final String TAG = UploadService.class.getSimpleName();

    public static final int JOB_ID  = 0x1400;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG, "Upload Service running");

        ServerConnector.UploadMessage uploadMessage = new ServerConnector.UploadMessage();

        ImageRollover imgroll = new ImageRollover(this, ".jpg");
        File newestImage = imgroll.getNewestImage();

        if (newestImage == null) {
            Log.i(TAG, "no image for upload available. abort.");

            jobFinished(jobParameters, false);
            return false;
        }

        uploadMessage.image = newestImage;

        Despat despat = ((Despat) getApplicationContext());
        SystemController systemController = despat.getSystemController();
        if (!systemController.isNetworkConnectionAvailable()) {
            Log.w(TAG, "no network connection available. abort.");
            return true;
        }

        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendUpload(uploadMessage);

        jobFinished(jobParameters, false); // <-- TODO: needs to be called from volley callback
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
