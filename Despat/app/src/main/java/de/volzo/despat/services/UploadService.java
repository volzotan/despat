package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import de.volzo.despat.ImageRollover;
import de.volzo.despat.ServerConnector;
import de.volzo.despat.SystemController;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 10.08.17.
 */

public class UploadService extends JobService {

    private static final String TAG = UploadService.class.getSimpleName();

    public static final int JOB_ID  = 0x1300;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG, "Upload Service running");

        ServerConnector.UploadMessage uploadMessage = new ServerConnector.UploadMessage();

        ImageRollover imgroll = new ImageRollover(Config.IMAGE_FOLDER, ".jpg");
        uploadMessage.image = imgroll.getNewestImage();

        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendUpload(uploadMessage);

        jobFinished(jobParameters, false); // <-- needs to be called from volley callback
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
