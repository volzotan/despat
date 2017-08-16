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
import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 15.08.17.
 */

public class Orchestrator extends BroadcastReceiver {

    public static final String TAG = Orchestrator.class.getSimpleName();

    public static final int OPERATION_START     = 1;
    public static final int OPERATION_STOP      = 2;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action       = intent.getAction();
        String service      = intent.getStringExtra("service");
        int operation       = intent.getIntExtra("operation", -1);

        Log.d(TAG, "Action: " + action + " | Service: " + service + " | operation: " + operation);

        if (action != null && action.length() > 0) switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                // TODO
                break;
            case "android.intent.action.SCREEN_OFF":
                // TODO
                break;
            case "android.intent.action.SCREEN_ON":
                // TODO
                break;
            default:
                Log.e(TAG, "invoked by unknown action");
                return;
        }

        switch (service) {
            case Broadcast.SHUTTER_SERVICE:
                if (operation == OPERATION_START) {

                    long now = System.currentTimeMillis(); // alarm is set right away

                    Intent shutterIntent = new Intent(context, Orchestrator.class);
                    shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
                    shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);
                    shutterIntent.putExtra("invocationTime", now);

                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                                ShutterService.REQUEST_CODE, shutterIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    // as of API lvl 19, all repeating alarms are inexact
                    // https://developer.android.com/reference/android/app/AlarmManager.html#setRepeating(int,%20long,%20long,%20android.app.PendingIntent)

                    long lastExecution = intent.getLongExtra("invocationTime", -1);
                    long correctionFactor = 0;
                    if (lastExecution > 0) {
                        correctionFactor = -1 * (now - lastExecution - Config.SHUTTER_INTERVAL);
                        // Log.d(TAG, "correctionFactor: " + correctionFactor);
                    }

                    long delay = now + Config.SHUTTER_INTERVAL + correctionFactor;
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delay, alarmIntent);
                } else if (operation == OPERATION_STOP) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                            ShutterService.REQUEST_CODE, new Intent(context, Orchestrator.class), PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.cancel(alarmIntent);
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
