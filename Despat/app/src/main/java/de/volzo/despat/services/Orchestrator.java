package de.volzo.despat.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.List;

import de.volzo.despat.RecordingSession;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.ServerConnector;
import de.volzo.despat.web.Sync;

/**
 * Created by volzotan on 15.08.17.
 */

public class Orchestrator extends BroadcastReceiver {

    public static final String TAG = Orchestrator.class.getSimpleName();

    public static final int OPERATION_START     = 1;
    public static final int OPERATION_STOP      = 2;
    public static final int OPERATION_ONCE      = 3;

    private Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {

        this.context = context;

//        try { // backup logcat entries
//            Util.backupLogcat(RecordingSession.getInstance(context).getSessionName());
//        } catch (RecordingSession.NotRecordingException e) {
//            Util.backupLogcat(null);
//        }

        if (Config.BACKUP_LOGCAT) {

            // during saving and clearing the log some
            // lines get lost if theres a lot of output happening

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.backupLogcat(null);
                }
            }, 500);
        }

        String action       = intent.getAction();
        String service      = intent.getStringExtra("service");
        int operation       = intent.getIntExtra("operation", -1);

        log(action, service, operation);

        if (action != null && action.length() > 0) {
            switch (action) {
                case "android.intent.action.BOOT_COMPLETED":
                    Util.saveEvent(context, Event.EventType.BOOT, null);

                    boolean resume = Config.getResumeAfterReboot(context);

                    if (resume) {
                        RecordingSession session = RecordingSession.getInstance(context);

                        try {
                            session.resumeRecordingSession();
                        } catch (Exception e) {
                            Log.e(TAG, "resuming failed", e);
                            // TODO: simply start a new one? in which cases could resuming fail? (empty db)
                        }

                        Config.setResumeAfterReboot(context, false); // TODO
                    }

                    break;
                case "android.intent.action.SCREEN_OFF": // TODO: doesn't work
                    Util.saveEvent(context, Event.EventType.DISPLAY_OFF, null);
                    Log.d(TAG, "+++ display off");
                    break;
                case "android.intent.action.SCREEN_ON":
                    Util.saveEvent(context, Event.EventType.DISPLAY_ON, null);
                    Log.d(TAG, "+++ display on");
                    break;

                case Broadcast.PICTURE_TAKEN:
                    try {
                        RecordingSession session = RecordingSession.getInstance(context);
                        if (session == null) {
                            Log.w(TAG, "image taken while no recordingSession is active");
                            break;
                        }
                        String path = intent.getStringExtra(Broadcast.DATA_PICTURE_PATH);
                        if (path != null) session.addCapture(new File(path));
                        Util.updateNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, session.getImagesTaken());
                    } catch (RecordingSession.NotRecordingException nre) {
                        Log.w(TAG, "resuming recording session failed");
                    }

                    break;

                default:
                    Log.e(TAG, "invoked by unknown action");
            }

            // processing of action finished, no service extra will be present
            return;
        }

        if (service == null) {
            Log.e(TAG, "invoked for unknown service");
            return;
        }

        switch (service) {
            case Broadcast.ALL_SERVICES:
                if (operation == OPERATION_START) {
                    shutterServiceStart();
                    recognitionServiceStart();
                    heartbeatServiceStart();
                    uploadServiceStart();
                    Log.i(TAG, "all services started");
                } else if (operation == OPERATION_STOP) {
                    shutterServiceStop();
                    recognitionServiceStop();
                    heartbeatServiceStop();
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

            case Broadcast.HEARTBEAT_SERVICE:
                if (operation == OPERATION_START) {
                    heartbeatServiceStart();
                } else if (operation == OPERATION_STOP) {
                    heartbeatServiceStop();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            case Broadcast.UPLOAD_SERVICE:
                if (operation == OPERATION_START) {
                    uploadServiceStart();
                } else if (operation == OPERATION_STOP) {
                    uploadServiceStop();
                } else if (operation == OPERATION_ONCE) {
                    uploadServiceOnce();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            default:
                Log.d(TAG, "unknown service to start: " + service);
        }
    }

    // ----------------------------------------------------------------------------------------------------

    private void shutterServiceStart() {

        // start the Shutter Service
        if (!Util.isServiceRunning(context, ShutterService.class)) {
            Intent shutterServiceIntent = new Intent(context, ShutterService.class);
            context.startService(shutterServiceIntent);

            Util.saveEvent(context, Event.EventType.INFO, "ShutterService START");
            Log.i(TAG, "ShutterService start");
        } else {
            Intent triggerIntent = new Intent();
            triggerIntent.setAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
            context.sendBroadcast(triggerIntent);
        }

        // check if sync should be run
        Sync.run(context, ShutterService.class, false);
        // TODO: this should be done in its own thread with its own wakelock

        // trigger the next invocation
        long now = System.currentTimeMillis(); // alarm is set right away

        Intent shutterIntent = new Intent(context, Orchestrator.class);
        shutterIntent.putExtra("service", Broadcast.SHUTTER_SERVICE);
        shutterIntent.putExtra("operation", Orchestrator.OPERATION_START);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                ShutterService.REQUEST_CODE, shutterIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long nextExecution = ((now + Config.getShutterInterval(context)) / 1000) * 1000;

        // save the time of the next invocation for the progressBar in the UI
        Config.setNextShutterServiceInvocation(context, nextExecution);

        /*
         * A note on alarms:
         *
         * as of API lvl 19, all repeating alarms are inexact,
         * so a single alarm needs to schedule the next one.
         * If that's the strategy of choice, this works for about
         * 60 to 70 minutes till Doze mode kicks in.
         * Upside: .andAllowWhileIdle(...) fires in Doze mode too
         * Downside: at most once every 9 minutes.
         *
         * So despat needs to be whitelisted on
         * settings > battery optimization
         *
         * */

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecution, alarmIntent);
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
    }

    // ----------------------------------------------------------------------------------------------------

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

    // ----------------------------------------------------------------------------------------------------

    private void heartbeatServiceStart() {
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
            builder.setPeriodic(Config.getHeartbeatInterval(context));
            jobScheduler.schedule(builder.build());
        } else {
            Log.d(TAG, "Heartbeat Service already scheduled");
        }
    }

    private void heartbeatServiceStop() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel(HeartbeatService.JOB_ID);

        // jobScheduler.cancelAll();
    }

    // ----------------------------------------------------------------------------------------------------

    private void uploadServiceStart() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);

        // already scheduled?
        boolean alreadyScheduled = false;
        List<JobInfo> allJobs = jobScheduler.getAllPendingJobs();
        for (JobInfo j : allJobs) {
            if (UploadService.JOB_ID == j.getId()) {
                alreadyScheduled = true;
                break;
            }
        }

        if (!alreadyScheduled) {
            ComponentName serviceComponent = new ComponentName(context, UploadService.class);
            JobInfo.Builder builder = new JobInfo.Builder(UploadService.JOB_ID, serviceComponent);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(Config.getUploadInterval(context), 30*1000);
            } else{
                builder.setPeriodic(Config.getUploadInterval(context));
            }

            jobScheduler.schedule(builder.build());
        } else {
            Log.d(TAG, "Upload Service already scheduled");
        }
    }

    private void uploadServiceStop() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel(UploadService.JOB_ID);

        // jobScheduler.cancelAll();
    }

    private void uploadServiceOnce() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);

        ComponentName serviceComponent = new ComponentName(context, UploadService.class);
        JobInfo.Builder builder = new JobInfo.Builder(UploadService.JOB_ID, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(1000);
        jobScheduler.schedule(builder.build());
    }


    private void log(String action, String service, int operation) {

        StringBuilder sb = new StringBuilder();
        sb.append(">> ");

        if (action != null) {
            sb.append("Action: ");
            sb.append(action.substring(action.lastIndexOf(".")));
        }

        if (service != null) {
            if (action != null) sb.append(" | ");
            sb.append("Service: ");
            sb.append(service.substring(service.lastIndexOf(".")));
        }

        if (operation >= 0) {
            if (service != null) sb.append(" | ");
            sb.append("operation: ");

            switch(operation) {
                case OPERATION_START:
                    sb.append("START");
                    break;
                case OPERATION_STOP:
                    sb.append("STOP");
                    break;
                case OPERATION_ONCE:
                    sb.append("ONCE");
                    break;
                default:
                    sb.append("UNKNOWN");
                    break;
            }
        }

        Log.d(TAG, sb.toString());
    }
}
