package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;

import de.volzo.despat.ServerConnector;
import de.volzo.despat.SystemController;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 10.08.17.
 */

public class HeartbeatService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        SystemController systemController = new SystemController(this);

        ServerConnector.ServerMessage serverMessage = new ServerConnector.ServerMessage();

        serverMessage.freeSpaceInternal = Util.getFreeSpaceOnDevice(Config.IMAGE_FOLDER);
        serverMessage.freeSpaceExternal = -1; // TODO
        serverMessage.batteryInternal = systemController.getBatteryLevel();
        serverMessage.batteryExternal = -1; // TODO
        serverMessage.stateCharging = systemController.getBatteryChargingState();

        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendStatus(serverMessage);

        jobFinished(jobParameters, false); // <-- needs to be called from volley callback
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
