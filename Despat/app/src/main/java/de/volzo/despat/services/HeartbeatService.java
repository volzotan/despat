package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import de.volzo.despat.Despat;
import de.volzo.despat.ImageRollover;
import de.volzo.despat.web.ServerConnector;
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

        ImageRollover imgroll = new ImageRollover(Config.getImageFolder(this), Config.IMAGE_FILEEXTENSION);

        ServerConnector.StatusMessage statusMessage = new ServerConnector.StatusMessage();

        statusMessage.numberImagesTaken = Config.getImagesTaken(this);
        statusMessage.numberImagesSaved = imgroll.getNumberOfSavedImages();
        statusMessage.freeSpaceInternal = Util.getFreeSpaceOnDevice(Config.getImageFolder(this));
        statusMessage.freeSpaceExternal = -1; // TODO
        statusMessage.batteryInternal = systemController.getBatteryLevel();
        statusMessage.batteryExternal = -1; // TODO
        statusMessage.stateCharging = systemController.getBatteryChargingState();

        // Temperature sensors are found in just a small number of devices, namely some Samsung 3 and Moto X
        // if a sensor is present, use the reading from the last heartbeat and start a new measurement for the next
        float temp = systemController.getTemperature();
        statusMessage.temperature = temp;
        if (temp > 0) {
            systemController.startTemperatureMeasurement();
        }

        if (!systemController.isNetworkConnectionAvailable()) {
            Log.w(TAG, "no network connection available. abort.");
            return true;
        }

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
