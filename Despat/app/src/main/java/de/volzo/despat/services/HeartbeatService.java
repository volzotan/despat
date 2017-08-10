package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;

/**
 * Created by volzotan on 10.08.17.
 */

public class HeartbeatService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
