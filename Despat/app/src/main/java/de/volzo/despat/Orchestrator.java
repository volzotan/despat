package de.volzo.despat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.volzo.despat.services.RecognitionService;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 15.08.17.
 */

public class Orchestrator extends BroadcastReceiver {

    public static final String TAG = Orchestrator.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        String service = intent.getStringExtra("service");
        String action = intent.getAction();

        Log.d(TAG, "Action: " + action + " | Service: " + service);

        if (action != null && action.length() > 0) switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                break;
            case "android.intent.action.SCREEN_OFF":
                break;
            case "android.intent.action.SCREEN_ON":
                break;
            default:
                Log.e(TAG, "invoked by unknown action");
                return;
        }

        switch (service) {
            case Broadcast.SHUTTER_SERVICE:
                // Construct an intent that will execute the AlarmReceiver
                Intent alarmIntent = new Intent(context, ShutterService.class);
                // Create a PendingIntent to be triggered when the alarm goes off
                final PendingIntent pIntent = PendingIntent.getBroadcast(context,
                        ShutterService.REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Setup periodic alarm every every half hour from this point onwards
                long firstMillis = System.currentTimeMillis(); // alarm is set right away
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
                // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);

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
                Log.d(TAG, "unknown service to start: " + service);
                return;
        }
    }
}
