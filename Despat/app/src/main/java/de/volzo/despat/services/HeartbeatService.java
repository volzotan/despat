package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import java.util.Calendar;

import de.volzo.despat.Despat;
import de.volzo.despat.ImageRollover;
import de.volzo.despat.RecordingSession;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;
import de.volzo.despat.web.ServerConnector;
import de.volzo.despat.SystemController;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.Sync;

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
        ImageRollover imgroll = new ImageRollover(this, ".jpg");

        // TODO: check if the RecordingSession is valid
        // (i.e. if the alarmManager was triggered always correctly)

        Status status = new Status();

        status.setTimestamp(Calendar.getInstance().getTime());
        status.setNumberImagesInMemory(imgroll.getNumberOfSavedImages()); // TODO: only JPEGs
        status.setFreeSpaceInternal(Util.getFreeSpaceOnDeviceInMb(Config.getImageFolder(this)));
        status.setFreeSpaceExternal(-1); // TODO
        status.setBatteryInternal(systemController.getBatteryLevel());
        status.setBatteryExternal(-1); // TODO
        status.setStateCharging(systemController.getBatteryChargingState());

        RecordingSession session = RecordingSession.getInstance(this);
        if (session.isActive()) {
            status.setNumberImagesTaken(session.getImagesTaken());
        }

        // Temperature sensors are found in just a small number of devices, namely some Samsung 3 and Moto X
        // if a sensor is present, use the reading from the last heartbeat and start a new measurement for the next
        float temp = systemController.getTemperature();
        status.setTemperature(temp);
        if (temp > 0) {
            systemController.startTemperatureMeasurement();
        }

        AppDatabase db = AppDatabase.getAppDatabase(despat);
        StatusDao statusDao = db.statusDao();
        statusDao.insert(status);

//        Util.startSyncManually(Util.createSyncAccount(this));
        Sync.run(this, HeartbeatService.class, false);

        jobFinished(jobParameters, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
