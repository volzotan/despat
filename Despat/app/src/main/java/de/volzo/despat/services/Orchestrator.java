package de.volzo.despat.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.volzo.despat.SessionManager;
import de.volzo.despat.detector.Detector;
import de.volzo.despat.detector.DetectorTensorFlowMobile;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.preferences.DetectorConfig;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.NotificationUtil;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.Sync;


public class Orchestrator extends BroadcastReceiver {

    private static final String TAG = Orchestrator.class.getSimpleName();

    public static final String SERVICE          = "service";
    public static final String OPERATION        = "operation";
    public static final String REASON           = "reason";

    public static final String BACKUP           = "backup";

    public static final int OPERATION_START     = 1;
    public static final int OPERATION_STOP      = 2;
    public static final int OPERATION_ONCE      = 3;

    public static final String ARG_SESSION_ID   = "ARG_SESSION_ID";

    private Context context;
    private CameraConfig cameraConfig;
    private String reason;

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
            // lines get lost if there is a lot of output happening

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.backupLogcat(null);
                }
            }, 500);
        }

        String action       = intent.getAction();
        String service      = intent.getStringExtra(SERVICE);
        final int operation = intent.getIntExtra(OPERATION, -1);
        reason              = intent.getStringExtra(REASON);

        log(action, service, operation, reason);

        if (service != null && (service.equals(Broadcast.ALL_SERVICES) || service.equals(Broadcast.SHUTTER_SERVICE))) {
            try {
                Long sessionId = intent.getLongExtra(Orchestrator.ARG_SESSION_ID, -1);
                Session session = null;

                if (sessionId != null && sessionId >= 0) { // TODO: does Room allow sessionId 0?
                    AppDatabase database = AppDatabase.getAppDatabase(context);
                    SessionDao sessionDao = database.sessionDao();
                    session = sessionDao.getById(sessionId);
                } else {
                    SessionManager sessionManager = SessionManager.getInstance(context);
                    session = sessionManager.getSession();
                }
                this.cameraConfig = session.getCameraConfig();

                if (cameraConfig == null) {
                    throw new Exception(String.format("empty camera config in session [%s]", session.toString()));
                }
            } catch (Exception e) {
                Log.e(TAG, "camera config missing. initialized with default values", e);
                this.cameraConfig = new CameraConfig(context);
            }

//            try {
//                this.cameraConfig = (CameraConfig) intent.getSerializableExtra(DATA_CAMERA_CONFIG);
//                if (this.cameraConfig == null) {
//                    Log.d(TAG, "camera config not stored in intent. Trying to get from session");
//
//                    SessionManager sessionManager = SessionManager.getInstance(context);
//                    this.cameraConfig = sessionManager.getCameraConfig();
//                } else {
//                    Log.wtf(TAG, "camera config received");
//                }
//            } catch (Exception e) {
//                Log.w(TAG, "camera config missing. initialized with default values");
//                this.cameraConfig = new CameraConfig(context);
//            }
        }

        if (action != null && action.length() > 0) {
            switch (action) {
                case "android.intent.action.BOOT_COMPLETED":
                    Util.saveEvent(context, Event.EventType.BOOT, null);

                    if (Config.getResumeAfterReboot(context)) {
                        SessionManager session = SessionManager.getInstance(context);
                        try {
                            session.resumeRecordingSession();
                            NotificationUtil.showStandardNotification(context, "resumed session: " + session.getSessionName());
                        } catch (Exception e) {
                            Log.i(TAG, "session not resumed: " + e.getMessage());
                        }
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

                case Broadcast.IMAGE_TAKEN:

                    String processName = Util.getProcessName(context);
                    if (processName == null) {
                        Log.e(TAG, "missing process name. Capture could not be saved");
                    } else if (!processName.equals("de.volzo.despat:despatShutterService")) {
                        return;
                    }

                    try {
                        SessionManager session = SessionManager.getInstance(context);
                        if (session == null) {
                            Log.w(TAG, "image taken while no recordingSession is active");
                            break;
                        }
                        CaptureInfo info = (CaptureInfo) intent.getSerializableExtra(Broadcast.DATA_IMAGE_CAPTUREINFO);
                        if (info != null) {
                            session.addCapture(info);
                        } else {
                            Log.w(TAG, "CaptureInfo/path missing. capture could not be saved");
                        }

                        ArrayList<String> addInfo = new ArrayList<>();
                        addInfo.add(Util.getHumanReadableTimediff(session.getStart(), Calendar.getInstance().getTime(), false));
                        NotificationUtil.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, session.getImagesTaken(), session.getErrors(), addInfo);

                        if (!Util.isServiceRunning(context, RecognitionService.class)) {
                            AppDatabase db = AppDatabase.getAppDatabase(context);
                            SessionDao sessionDao = db.sessionDao();
                            Session newestSession = sessionDao.getLast();
                            if (newestSession != null) {
                                Orchestrator.runRecognitionService(context, newestSession.getId());
                            }
                        } else {
                            Log.d(TAG, "RecognitionService not started, already running");
                        }

                        if (!Util.isServiceRunning(context, CompressorService.class)) {
                            AppDatabase db = AppDatabase.getAppDatabase(context);
                            SessionDao sessionDao = db.sessionDao();
                            Session newestSession = sessionDao.getLast();
                            if (newestSession != null) {
                                Orchestrator.runCompressorService(context, newestSession.getId()); // TODO: only add newest images
                            }
                        } else {
                            Log.d(TAG, "CompressorService not started, already running");
                        }

                    } catch (SessionManager.NotRecordingException nre) {
                        Log.w(TAG, "resuming recording session failed");
                    }
                    break;

                case Broadcast.NEXT_SHUTTER_INVOCATION:
                    Config.setNextShutterServiceInvocation(context, intent.getLongExtra(Broadcast.DATA_TIME, -1));
                    break;

                case Broadcast.ERROR_OCCURED:
                    try {
                        SessionManager session = SessionManager.getInstance(context);
                        if (session == null) {
                            Log.w(TAG, "error occured while no recordingSession is active");
                            break;
                        }

                        // save error event
                        String desc = null;
                        Throwable e = null;

                        try {
                            desc = intent.getStringExtra(Broadcast.DATA_DESCRIPTION);
                            e = intent.getParcelableExtra(Broadcast.DATA_THROWABLE);
                        } catch (Exception ve) {
                            Log.w(TAG, "unpacking error broadcast failed", ve);
                        }

                        session.addError(desc, e);

                        // backup logfile
                        Util.backupLogcat(null);

                        // log
                        Log.e(TAG, "error occured: " + desc, e);

                        NotificationUtil.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, session.getImagesTaken(), session.getErrors(), null);
                    } catch (SessionManager.NotRecordingException nre) {
                        Log.w(TAG, "resuming recording session failed");
                    }
                    break;

                case Broadcast.COMMAND_RUN_BENCHMARK:
                    Log.wtf(TAG, "COMMAND RUN BENCHMARK");

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Creating Benchmarks started");
                            try {
                                for (String fidelity : DetectorTensorFlowMobile.FIDELITY_MODE) {
                                    Detector detector = new DetectorTensorFlowMobile(context, new DetectorConfig(fidelity, 1000)); // TODO: tilesize 1000 for all detectors? Is that overwritten somewhere?
                                    detector.init();
                                    Long time = ((DetectorTensorFlowMobile) detector).estimateComputationTime(new Size(1000, 1000));

                                    // if no time could be estimated, there no/not enough Benchmarks in the db
                                    if (time == null || operation > 0) {
                                        if (operation > 0) {
                                            Log.i(TAG, "running benchmark by force");
                                        }

                                        detector.runBenchmark();
                                    } else {
                                        Log.e(TAG, "no benchmark required");
                                    }
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Creating Benchmarks failed", e);
                            }
                        }
                    });

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

        boolean isBackup = intent.getBooleanExtra(BACKUP, false);
        if (isBackup) {
            Log.wtf(TAG, "backup alarm called");
            Util.saveErrorEvent(context, "backup alarm has been invoked", null);

            // cancel main alarm

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                    context,
                    ShutterService.REQUEST_CODE_2,
                    new Intent(context, Orchestrator.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(alarmIntent);
            alarmIntent.cancel();
        }

        switch (service) {
            case Broadcast.ALL_SERVICES:
                if (operation == OPERATION_START) {
                    shutterServiceStart();
                    recognitionServiceStart();
                    heartbeatServiceStart();
                    uploadServiceStart();
                    locationServiceStart();
                    Log.i(TAG, "all services started");
                } else if (operation == OPERATION_STOP) {
                    shutterServiceStop();
                    recognitionServiceStop();
                    heartbeatServiceStop();
                    uploadServiceStop();
                    locationServiceStop();
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
                } else if (operation == OPERATION_ONCE) {
                    heartbeatServiceOnce();
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

            case Broadcast.LOCATION_SERVICE:
                if (operation == OPERATION_START) {
                    locationServiceStart();
                } else if (operation == OPERATION_STOP) {
                    locationServiceStop();
                } else if (operation == OPERATION_ONCE) {
                    locationServiceOnce();
                } else {
                    Log.w(TAG, "no operation command provided");
                }
                break;

            default:
                Log.d(TAG, "unknown service to start: " + service);
        }
    }

    public static void runCompressorService(Context context) {
        Log.d(TAG, "CompressorService started");

        if (Util.isServiceRunning(context, CompressorService.class)) {
            Log.w(TAG, "CompressorService already running");
        }

        Intent compressorIntent = new Intent(context, CompressorService.class);
        context.startService(compressorIntent);
    }

    public static void runCompressorService(Context context, long sessionId) {
        Log.d(TAG, "CompressorService started (for single session)");

        if (Util.isServiceRunning(context, CompressorService.class)) {
            Log.w(TAG, "CompressorService already running");
        }

        Intent compressorIntent = new Intent(context, CompressorService.class);
        compressorIntent.putExtra(CompressorService.ARG_SESSION_ID, sessionId);
        context.startService(compressorIntent);
    }

    public static void runHomographyService(Context context, long sessionId) {
        Log.d(TAG, "HomographyService started");

        if (Util.isServiceRunning(context, HomographyService.class)) {
            Log.w(TAG, "HomographyService already running");
        }

        Intent homographyIntent = new Intent(context, HomographyService.class);
        homographyIntent.putExtra(HomographyService.ARG_SESSION_ID, sessionId);
        context.startService(homographyIntent);
    }

    public static void runRecognitionService(Context context, long sessionId) {
        Log.d(TAG, "RecognitionService started");

        if (Util.isServiceRunning(context, RecognitionService.class)) {
            Log.w(TAG, "RecognitionService already running");
        }

        Intent recognitionIntent = new Intent(context, RecognitionService.class);
        recognitionIntent.putExtra(RecognitionService.ARG_SESSION_ID, sessionId);
        context.startService(recognitionIntent);
    }

    // ----------------------------------------------------------------------------------------------------

    private void shutterServiceStart() {

        // start the Shutter Service
        if (!Util.isServiceRunning(context, ShutterService.class)) {
            Intent shutterServiceIntent = new Intent(context, ShutterService.class);
//            Bundle args = new Bundle();
//            args.putSerializable(ShutterService.ARG_CAMERA_CONFIG, cameraConfig);
//            shutterServiceIntent.putExtras(args);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(shutterServiceIntent);
                } else {
                    context.startService(shutterServiceIntent);
                }

                Util.saveEvent(context, Event.EventType.INFO, "ShutterService START");
                Log.i(TAG, "ShutterService start");

                Util.showSnackbar(context, "Recording started", reason);
            } catch (IllegalStateException e) {
                String msg = "ShutterService could not be started (probably due to background restrictions";
                Log.e(TAG, msg);
                Util.saveErrorEvent(context, msg, e);
            }
        } else {
            Intent triggerIntent = new Intent();
            triggerIntent.setAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
            context.sendBroadcast(triggerIntent);
        }

        // check if sync should be run
        Sync.run(context, ShutterService.class, false);
        // TODO: this should be done in its own thread with its own wakelock

        if (!this.cameraConfig.getPersistentCamera()) {

            // trigger the next invocation
            long now = System.currentTimeMillis(); // alarm is set right away

            Intent shutterIntent = new Intent(context, Orchestrator.class);
            shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
            shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
            shutterIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // not sure if actually needed

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
                    ShutterService.REQUEST_CODE,
                    shutterIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long nextExecution = now + cameraConfig.getShutterInterval();
            nextExecution -= nextExecution % 1000;

            // save the time of the next invocation for the progressBar in the UI
            Intent nextInvocationIntent = new Intent(Broadcast.NEXT_SHUTTER_INVOCATION);
            nextInvocationIntent.putExtra(Broadcast.DATA_TIME, nextExecution);
            context.sendBroadcast(nextInvocationIntent);

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

//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextExecution, alarmIntent);

            nextExecution = SystemClock.elapsedRealtime() + cameraConfig.getShutterInterval();
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextExecution, alarmIntent);

            // cancel backup

            alarmIntent = PendingIntent.getBroadcast(
                    context,
                    ShutterService.REQUEST_CODE_2,
                    new Intent(context, Orchestrator.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(alarmIntent);
            alarmIntent.cancel();

            // and schedule a new 2nd alarm for backup:

            shutterIntent = new Intent(context, Orchestrator.class);
            shutterIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
            shutterIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_START);
            shutterIntent.putExtra(Orchestrator.BACKUP, true);
            shutterIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

            alarmIntent = PendingIntent.getBroadcast(context,
                    ShutterService.REQUEST_CODE_2,
                    shutterIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            nextExecution = SystemClock.elapsedRealtime() + cameraConfig.getShutterInterval() * 2;
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextExecution, alarmIntent);
        }
    }

    private void shutterServiceStop() {
        if (cameraConfig.getPersistentCamera()) {

        } else {
            // alarm Manager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                    context,
                    ShutterService.REQUEST_CODE,
                    new Intent(context, Orchestrator.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(alarmIntent);
            alarmIntent.cancel();

            // cancel backup
            alarmIntent = PendingIntent.getBroadcast(
                    context,
                    ShutterService.REQUEST_CODE_2,
                    new Intent(context, Orchestrator.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(alarmIntent);
            alarmIntent.cancel();
        }

        // shutter Service
        Intent shutterServiceIntent = new Intent(context, ShutterService.class);
        context.stopService(shutterServiceIntent);

        Util.showSnackbar(context, "Recording stopped", reason);
    }

    // ---------------------------------------------------------------------------------------------

    private void recognitionServiceStart() {

        // TODO

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

    // ---------------------------------------------------------------------------------------------

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(Config.getHeartbeatInterval(context), 30*1000);
            } else{
                builder.setPeriodic(Config.getHeartbeatInterval(context));
            }

            jobScheduler.schedule(builder.build());
        } else {
            Log.d(TAG, "Heartbeat Service already scheduled");
        }
    }

    private void heartbeatServiceOnce() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        ComponentName serviceComponent = new ComponentName(context, HeartbeatService.class);
        JobInfo.Builder builder = new JobInfo.Builder(HeartbeatService.JOB_ID, serviceComponent);
        builder.setMinimumLatency(1);
        builder.setOverrideDeadline(1);
        jobScheduler.schedule(builder.build());
    }

    private void heartbeatServiceStop() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel(HeartbeatService.JOB_ID);
    }

    // ---------------------------------------------------------------------------------------------

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

    // ---------------------------------------------------------------------------------------------

    private void locationServiceStart() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);

        // already scheduled?
        boolean alreadyScheduled = false;
        List<JobInfo> allJobs = jobScheduler.getAllPendingJobs();
        for (JobInfo j : allJobs) {
            if (LocationService.JOB_ID == j.getId()) {
                alreadyScheduled = true;
                break;
            }
        }

        if (!alreadyScheduled) {
            ComponentName serviceComponent = new ComponentName(context, LocationService.class);
            JobInfo.Builder builder = new JobInfo.Builder(LocationService.JOB_ID, serviceComponent);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(Config.getLocationInterval(context), 30*1000);
            } else{
                builder.setPeriodic(Config.getLocationInterval(context));
            }

            jobScheduler.schedule(builder.build());
        } else {
            Log.d(TAG, "Location Service already scheduled");
        }
    }

    private void locationServiceStop() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancel(LocationService.JOB_ID);

        // jobScheduler.cancelAll();
    }

    private void locationServiceOnce() {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);

        ComponentName serviceComponent = new ComponentName(context, LocationService.class);
        JobInfo.Builder builder = new JobInfo.Builder(LocationService.JOB_ID, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setMinimumLatency(0);
        builder.setOverrideDeadline(1000);
        jobScheduler.schedule(builder.build());
    }

    // ---------------------------------------------------------------------------------------------

    private void log(String action, String service, int operation, String reason) {

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

        if (reason != null) {
            sb.append(" | ");
            sb.append("(");
            sb.append(reason);
            sb.append(")");
        }

        Log.d(TAG, sb.toString());
    }
}
