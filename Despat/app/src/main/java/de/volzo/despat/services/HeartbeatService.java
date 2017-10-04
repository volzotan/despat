package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import de.volzo.despat.Despat;
import de.volzo.despat.ServerConnector;
import de.volzo.despat.SystemController;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 10.08.17.
 */

public class HeartbeatService extends JobService {

    private static final String TAG = HeartbeatService.class.getSimpleName();

    public static final int JOB_ID  = 0x1300;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG, "Heartbeat Service running");

        Despat despat = ((Despat) getApplicationContext());
        SystemController systemController = despat.getSystemController();

        ServerConnector.StatusMessage statusMessage = new ServerConnector.StatusMessage();

        statusMessage.numberImages = ((Despat) getApplicationContext()).getImagesTaken();
        statusMessage.freeSpaceInternal = Util.getFreeSpaceOnDevice(Config.IMAGE_FOLDER);
        statusMessage.freeSpaceExternal = -1; // TODO
        statusMessage.batteryInternal = systemController.getBatteryLevel();
        statusMessage.batteryExternal = -1; // TODO
        statusMessage.stateCharging = systemController.getBatteryChargingState();
        statusMessage.temperature = systemController.getTemperature();

        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendStatus(statusMessage);

        jobFinished(jobParameters, false); // <-- needs to be called from volley callback
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
