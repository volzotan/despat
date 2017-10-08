package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import de.volzo.despat.web.ServerConnector;

/**
 * Created by volzotan on 10.08.17.
 */

public class CommandService extends JobService {

    private static final String TAG = CommandService.class.getSimpleName();

    public static final int JOB_ID  = 0x1500;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG, "Command Service running");

        ServerConnector.UploadMessage uploadMessage = new ServerConnector.UploadMessage();

//        ImageRollover imgroll = new ImageRollover(Config.IMAGE_FOLDER, ".jpg");
//        uploadMessage.image = imgroll.getNewestImage();
//
//        ServerConnector serverConnector = new ServerConnector(this);
//        serverConnector.sendUpload(uploadMessage);

        /*

        Commands:

        STOP shutter
        START shutter
        OFF
        REBOOT

        STOP uploading
        START uploading
        UPLOAD single picture

        CLEAR imageroll

         */



        jobFinished(jobParameters, false); // <-- needs to be called from volley callback
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
