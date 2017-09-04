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

import java.util.List;

import de.volzo.despat.services.HeartbeatService;
import de.volzo.despat.services.RecognitionService;
import de.volzo.despat.services.ShutterService;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 15.08.17.
 */

public class Orchestrator extends BroadcastReceiver {

    public static final String TAG = Orchestrator.class.getSimpleName();

    public static final int OPERATION_START     = 1;
    public static final int OPERATION_STOP      = 2;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

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
            case Broadcast.ALL_SERVICES:
                if (operation == OPERATION_START) {
                    shutterServiceStart();
                    recognitionServiceStart();
                    uploadServiceStart();
                    Log.i(TAG, "all services started");
                } else if (operation == OPERATION_STOP) {
                    shutterServiceStop();
                    recognitionServiceStop();
                    uploadServiceStop();
                    Log.i(TAG, "all running services stopped");
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            case Broadcast.SHUTTER_SERVICE:
                if (operation == OPERATION_START) {
                    shutterServiceStart();
                } else if (operation == OPERATION_STOP) {
                    shutterServiceStop();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            case Broadcast.RECOGNITION_SERVICE:
                if (operation == OPERATION_START) {
                    recognitionServiceStart();
                } else if (operation == OPERATION_STOP) {
                    recognitionServiceStop();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            case Broadcast.UPLOAD_SERVICE:
                if (operation == OPERATION_START) {
                    uploadServiceStart();
                } else if (operation == OPERATION_STOP) {
                    uploadServiceStop();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            default:
                Log.d(TAG, "unknown service to start: " + service);
                return;
        }
    }

    private void shutterServiceStart() {

        // start the Shutter Service
        if (!Util.isServiceRunning(context, ShutterService.class)) {
            Intent shutterServiceIntent = new Intent(context, ShutterService.class);
            context.startService(shutterServiceIntent);
        }

        Intent triggerIntent = new Intent();
        triggerIntent.setAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        context.sendBroadcast(triggerIntent);

        // trigger the next invocation
        long now = System.currentTimeMillis(); // alarm is set right away

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                ShutterService.REQUEST_CODE, shutterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long nextExecution = ((now + Config.SHUTTER_INTERVAL) / 1000) * 1000;

        // as of API lvl 19, all repeating alarms are inexact,
        // so a single alarm needs to schedule the next one
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecution, alarmIntent);

        Despat despat = ((Despat) context.getApplicationContext());
        Util.startNotification(context, despat.getImagesTaken());
    }

    private void shutterServiceStop() {
        // alarm Manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                ShutterService.REQUEST_CODE, new Intent(context, Orchestrator.class), PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();

        // shutter Service
        Intent shutterServiceIntent = new Intent(context, ShutterService.class);
        context.stopService(shutterServiceIntent);

        // Notification
        Util.stopNotification(context);
    }

    private void recognitionServiceStart() {
        ComponentName serviceComponent = new ComponentName(context, RecognitionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(3 * 1000); // wait at least
        builder.setOverrideDeadline(5 * 1000); // maximum delay
        //builder.setRequiresDeviceIdle(true); // device should be idle
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    private void recognitionServiceStop() {
        // TODO
    }

    private void uploadServiceStart() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);

        // already scheduled?
        boolean alreadyScheduled = false;
        List<JobInfo> allJobs = jobScheduler.getAllPendingJobs();
        for (JobInfo j : allJobs) {
            if (HeartbeatService.JOB_ID == j.getId()) {
                alreadyScheduled = true;
                break;
            }
        }

        if (!alreadyScheduled) {
            ComponentName serviceComponent = new ComponentName(context, HeartbeatService.class);
            JobInfo.Builder builder = new JobInfo.Builder(HeartbeatService.JOB_ID, serviceComponent);
            builder.setPeriodic(15 * 60 * 1000L); // Minimum interval is 15m
            jobScheduler.schedule(builder.build());
        } else {
            Log.d(TAG, "Heartbeat Service already scheduled");
        }
    }

    private void uploadServiceStop() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel(HeartbeatService.JOB_ID);

        // jobScheduler.cancelAll();
    }
}
