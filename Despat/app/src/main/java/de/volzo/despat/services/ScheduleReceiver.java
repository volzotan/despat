package de.volzo.despat.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.08.17.
 */

public class ScheduleReceiver extends BroadcastReceiver {

    public static final String TAG = ScheduleReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action == null) {
            Log.d(TAG, "BroadcastReceiver called by empty intent");
            return;
        }


        Log.d(TAG, "Received: " + intent.getAction());
        switch (action) {
            case Broadcast.SHUTTER_SERVICE:
                break;

            case Broadcast.RECOGNITION_SERVICE:

                ComponentName serviceComponent = new ComponentName(context, RecognitionService.class);
                JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
                builder.setMinimumLatency(3* 1000); // wait at least
                builder.setOverrideDeadline(5 * 1000); // maximum delay
                //builder.setRequiresDeviceIdle(true); // device should be idle
                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                jobScheduler.schedule(builder.build());

                break;

            case Broadcast.UPLOAD_SERVICE:
                // TODO
                break;

            default:
                Log.d(TAG, "BroadcastReceiver called by unknown intent: " + action);
                return;
        }

        Log.i(TAG, "Starting service: " + action);
    }
}
