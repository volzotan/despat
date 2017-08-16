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

    public static final int OPERATION_START     = 1;
    public static final int OPERATION_STOP      = 2;

    AlarmManager alarmManager;
    PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action       = intent.getAction();
        String service      = intent.getStringExtra("service");
        int operation       = intent.getIntExtra("operation", -1);

        Log.d(TAG, "Action: " + action + " | Service: " + service + " | operation: " + operation);

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
                if (operation == OPERATION_START) {
                    Intent shutterIntent = new Intent(context, ShutterService.class);
                    if (alarmIntent == null) {
                        alarmIntent = PendingIntent.getBroadcast(context,
                                ShutterService.REQUEST_CODE, shutterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    long firstMillis = System.currentTimeMillis(); // alarm is set right away
                    if (alarmManager == null){
                        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    }
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, 60 * 1000, alarmIntent);
                } else if (operation == OPERATION_STOP) {
                    if (alarmManager!= null) {
                        alarmManager.cancel(alarmIntent);
                    }
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            case Broadcast.RECOGNITION_SERVICE:
                if (operation == OPERATION_START) {
                    ComponentName serviceComponent = new ComponentName(context, RecognitionService.class);
                    JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
                    builder.setMinimumLatency(3 * 1000); // wait at least
                    builder.setOverrideDeadline(5 * 1000); // maximum delay
                    //builder.setRequiresDeviceIdle(true); // device should be idle
                    JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                    jobScheduler.schedule(builder.build());
                } else if (operation == OPERATION_STOP) {
                    // TOOD
                } else {
                    Log.w(TAG, "no operation command provided");
                }
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
