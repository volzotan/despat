package de.volzo.despat.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.File;
import java.util.Calendar;

import de.volzo.despat.Despat;
import de.volzo.despat.SessionManager;
import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.DeviceLocation;
import de.volzo.despat.persistence.DeviceLocationDao;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.ServerConnector;


public class LocationService extends JobService {

    private static final String TAG = LocationService.class.getSimpleName();

    public static final int JOB_ID  = 0x1600;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG, "Location Service running");

        final Context context = this;

        Despat despat = Util.getDespat(context);
        SystemController systemController = despat.getSystemController();
        systemController.getLocation(new SystemController.LocationCallback() {
            @Override
            public void locationAcquired(Location location) {

                try {
                    if (location == null) {
                        throw new Exception("location data missing");
                    }

                    AppDatabase db = AppDatabase.getAppDatabase(context);
                    DeviceLocationDao deviceLocationDao = db.deviceLocationDao();
                    DeviceLocation loc = new DeviceLocation();

                    loc.setTimestamp(Calendar.getInstance().getTime());
                    loc.setLocation(location);

                    deviceLocationDao.insert(loc);
                } catch (Exception e) {
                    Log.e(TAG, "location saving failed");
                } finally {

                }

            }
        });

        jobFinished(jobParameters, false);
        return false;
    }



    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
