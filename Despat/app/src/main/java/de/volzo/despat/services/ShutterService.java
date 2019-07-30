package de.volzo.despat.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import de.volzo.despat.CameraController;
import de.volzo.despat.Despat;
import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Session;
import de.volzo.despat.persistence.SessionDao;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.SessionManager;
import de.volzo.despat.SystemController;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.NotificationUtil;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.Sync;


public class ShutterService extends Service {

    /**
     * A word of caution:
     *
     * running the ShutterService in its own process
     * ( via defining android:process=":despatShutterService" in AndroidManifest.xml" )
     * will yield inconsistent states when accessing sharedPreferences. The process obtains
     * a memory cached copy at startup and ignores any further changes. The app needs to be
     * restarted to update the values the process accesses.
     *
     * (SharedPref docs: "Note: This class does not support use across multiple processes." )
     *
     */

    private static final String TAG = ShutterService.class.getSimpleName();

    public static final String ARG_CAMERA_CONFIG        = "ARG_CAMERA_CONFIG";

    public static final int REQUEST_CODE                = 0x0100;
    public static final int REQUEST_CODE_2              = 0x0101;
    public static final int FOREGROUND_NOTIFICATION_ID  = 0x0200;
    public static final String NOTIFICATION_CHANNEL_ID  = "de.volzo.despat.notificationchannel.ShutterService";

    private static final int STATE_INIT                 = 0;

    private static final int STATE_CAMERA_READY         = 1;
    private static final int STATE_BUSY                 = 2;

    private static final int STATE_ERROR                = 3;
    private static final int STATE_RESTART              = 4;
    private static final int STATE_SHUTDOWN             = 5;

    private static final int STATE_SECOND_IMAGE         = 6;
    private static final int STATE_SECOND_IMAGE_BUSY    = 7;

    Context context;
    Handler handler;
    CameraConfig camconfig;

    CameraController camera;

    private int state = STATE_INIT;
    private List<Long> cameraRestarts;

    public ShutterService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // ---------------------------------------------------------------------------------------------

    Runnable cameraWatchdogRunnable = new Runnable() {
        @Override
        public void run() {

            if (Config.getPersistentCamera(context)) {
                // TODO: reschedule new shutterRelease? restart camera?
                // TODO: has a new picture been taken? is everything in its orderly fashion?
            } else {

                // on calling the watchdog the camera is still alive
                // the ShutterService failed to close the camera in time.

                Log.wtf(TAG, "CAMERA WATCHDOG KILL");
                Util.saveEvent(context, Event.EventType.ERROR, "camera: watchdog kill");

                Despat despat = Util.getDespat(context);
                despat.closeCamera();
            }
        }
    };

    Runnable shutterReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            Intent triggerIntent = new Intent();
            triggerIntent.setAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
            context.sendBroadcast(triggerIntent);
        }
    };

    // ---------------------------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.wtf(TAG, "ShutterService is running on UI thread");
        } else {
            Log.d(TAG, "ShutterService is running on thread: " + Thread.currentThread().getName());
        }

        Log.d(TAG, "SHUTTER SERVICE invoked");

        this.context = this;
        this.handler = new Handler();

        try {
//            Bundle args = intent.getExtras();
//            this.camconfig = (CameraConfig) args.getSerializable(ARG_CAMERA_CONFIG);

            AppDatabase db = AppDatabase.getAppDatabase(context);
            SessionDao sessionDao = db.sessionDao();
            Session session = sessionDao.getLast();
            this.camconfig = session.getCameraConfig();
        } catch (Exception e) {
            Log.e(TAG, "Camera Config missing");
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, Throwable arg1) {

                StringBuilder sb = new StringBuilder();
                sb.append("ShutterService uncaught exception");

                if (arg0 != null) {
                    sb.append(" | Thread: ");
                    sb.append(arg0.getName());
                }

                if (arg1 != null) {
                    sb.append(" | Exception: ");
                    sb.append(arg1.getMessage());

                    Throwable cause = arg1.getCause();
                    if (cause != null) {
                        sb.append(" | Cause: ");
                        sb.append(cause);
                        sb.append(" ");
                        sb.append(cause.getMessage());
                    }
                }

                Log.e(TAG, sb.toString());

                if (arg1 != null) arg1.printStackTrace();

                Util.saveErrorEvent(context, "ShutterService uncaught exception", arg1);
                Util.backupLogcat(null);

                // TODO: how to proceed? restart?
            }
        });

        startForeground(FOREGROUND_NOTIFICATION_ID, NotificationUtil.getShutterNotification(context, 0, 0, null));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.SHUTTER_SERVICE_TRIGGER);
        registerReceiver(shutterReceiver, filter);

        // TODO: receiving these broadcasts as the shutterService can lead to critical app crashes
//        filter = new IntentFilter();
//        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
//        filter.addAction("android.intent.action.SCREEN_OFF");
//        filter.addAction("android.intent.action.SCREEN_ON");
//        registerReceiver(powerReceiver, filter);

        Despat despat = Util.getDespat(this);

//        if (true) throw new RuntimeException("testcrash");

        if (Config.getPersistentCamera(context)) {
            despat.acquireWakeLock(false);

            try {
                startCamera();
            } catch (Exception e) {
                state = STATE_ERROR;
                eventMalfunction("error during opening camera", e);

                // if the service fails to start correctly
                // it should not be restarted automatically

                return START_NOT_STICKY;
            }
        } else {
            handler.post(shutterReleaseRunnable);
        }

        Log.d(TAG, "Shutter service started successfully");

        return START_STICKY;
    }

    // ---------------------------------------------------------------------------------------------

    private void startCamera() throws Exception {

        final Despat despat = Util.getDespat(this);

        // check if any images needs to be deleted to have enough free space
        // TODO: may be time-consuming. alternative place to run?
        ImageRollover imgroll = new ImageRollover(despat, null);
        imgroll.run();

        camera = despat.getCamera();

        try {
            if (camera == null || camera.isDead()) {
                Log.d(TAG, "CamController created");
                camera = despat.initCamera(this, cameraCallback, null, camconfig);
                camera.openCamera();
            } else {
                Log.d(TAG, "CamController already up and running");
                eventCameraReady();
            }

        } catch (Exception e) {
            // opening camera failed
            // calling entity needs to decide
            // if a restart may be necessary
            throw e;
        }
    }

    private void restartCamera() {
        Log.i(TAG, "camera restart");
        state = STATE_RESTART;
        shutdownCamera();
    }

    private void restartCameraAfterMalfunction() {
        Log.w(TAG, "camera restart after malfunction");

        // remove all restart timestamps which are too old
        if (cameraRestarts == null) cameraRestarts = new ArrayList<Long>();
        List<Long> rmList = new ArrayList<Long>();
        for (Long t : cameraRestarts) {
            long diff = System.currentTimeMillis() - t;
            if (diff > Config.CAMERA_RESTART_TIME_WINDOW) rmList.add(t);
        }
        if (rmList.size() > 0) cameraRestarts.removeAll(rmList);

        if (cameraRestarts.size() < Config.CAMERA_RESTART_MAX_NUMBER) {

            cameraRestarts.add(System.currentTimeMillis());
            Util.saveEvent(this, Event.EventType.ERROR, "camera restart after malfunction");

            // purge all scheduled trigger events as to not interrupt while the camera is not ready
            // When the cmaera has restarted, it will schedule a new one
            handler.removeCallbacksAndMessages(null);
            restartCamera();

        } else {
            Log.w(TAG, String.format(Config.LOCALE, "camera restart after malfunction aborted. " +
                    "%d restarts happened in the last %dms", cameraRestarts.size(), cameraRestarts.get(cameraRestarts.size()-1)));

            // TODO: critical error?

            // do nothing
            state = STATE_ERROR;
            abortAfterMalfunction();
            return;
        }
    }

    private void triggerCamera() {
        if (state == STATE_SECOND_IMAGE) {
            try {
                state = STATE_SECOND_IMAGE_BUSY;
                camera.startMetering(camconfig.getSecondImageExposureCompensation());
            } catch (Exception e) {
                eventMalfunction("starting metering failed (2nd image)", e);
                restartCameraAfterMalfunction();
            }
        } else if (Config.RERUN_METERING_BEFORE_CAPTURE) {
            try {
                state = STATE_BUSY;
                camera.startMetering(camconfig.getExposureCompensation());
            } catch (Exception e) {
                eventMalfunction("starting metering failed", e);
                restartCameraAfterMalfunction();
            }
        } else {
            try {
                state = STATE_BUSY;
                camera.captureImages(null);
            } catch (Exception e) {
                eventMalfunction("capturing image failed", e);
            }
        }
    }

    private void shutdownCamera() {
        Despat despat = Util.getDespat(context);
        despat.closeCamera();
    }

    private void shutdownService() {

    }

    private void abortAfterMalfunction() {
        Log.e(TAG, "abort after malfunction");

        ArrayList<String> addInfo = new ArrayList<String>();
        addInfo.add("ShutterService aborted after restarting failed");
        NotificationUtil.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, 0, 1, addInfo);

        if (Config.getPersistentCamera(context)) {
            handler.removeCallbacksAndMessages(null);
        } else {
            // TODO
        }

        Util.saveEvent(this, Event.EventType.ERROR, "abort after malfunction");
    }

    // ---------------------------------------------------------------------------------------------

    private void eventCameraReady() {

        if (Config.getPersistentCamera(context)) {

            switch (state) {
                case STATE_INIT: {
                    state = STATE_CAMERA_READY;
                    handler.post(shutterReleaseRunnable);
                    break;
                }
                case STATE_RESTART: {
                    // existing scheduled shutter events have been killed for restart,
                    // schedule a new one
                    handler.post(shutterReleaseRunnable);
                    break;
                }
                default: {
                    eventMalfunction("invalid state in eventCameraReady: " + state);
                }
            }

        } else {
            triggerCamera();
        }
    }

    private void eventCaptureComplete(CaptureInfo info) {

        if (info == null) {
            Log.e(TAG, "CaptureInfo empty");
        }

        if (state != STATE_SECOND_IMAGE_BUSY && camconfig.getSecondImageExposureCompensation() != 0) {
            if (info != null && Config.getExposureThreshold(context) > 1.0) {
                double exposureValue = Util.computeExposureValue(info.getExposureTime(), info.getAperture(), info.getIso());
                if (exposureValue <= Config.getExposureThreshold(context)) {
                    state = STATE_SECOND_IMAGE;
                    handler.post(shutterReleaseRunnable);
                    return;
                }

                // else: camera ready

            } else { // info is null: take second image
                state = STATE_SECOND_IMAGE;
                handler.post(shutterReleaseRunnable);
                return;
            }
        }

        state = STATE_CAMERA_READY;
        if (!Config.getPersistentCamera(context)) {
            shutdownCamera();
        }
    }


    private void eventMalfunction(String message, Object o) {

        // Something dire happened

        // create error message
        StringBuilder sb = new StringBuilder();
        sb.append("ShutterService failed: ");
        sb.append(message);

        Throwable e = null;

        try {
            e = (Throwable) o;
        } catch (Exception castException) {
            // do nothing
        }

        sb.append(" | ");
        if (e != null) {
            e = (Throwable) o;
            sb.append(e.getMessage());
            sb.append(" --- ");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sb.append(sw.toString());
        } else if (o != null){
            sb.append(o.toString());
        }

        Log.e(TAG, sb.toString());

        // persist error
        Util.saveEvent(this, Event.EventType.ERROR, sb.toString());
        Util.saveErrorEvent(this, "generic ShutterService malfunction: " + message, e);

        // send error broadcast
        Intent intent = new Intent(Broadcast.ERROR_OCCURED);
        intent.putExtra(Broadcast.DATA_DESCRIPTION, message);
        // TODO pack throwable in parcel
        // if (e != null) intent.putExtra(Broadcast.DATA_THROWABLE, e);
        context.sendBroadcast(intent);

        // notify user
        Toast.makeText(context, "ShutterService failed: " + message, Toast.LENGTH_SHORT).show();

        ArrayList<String> addInfo = new ArrayList<String>();
        addInfo.add("ShutterService crashed");
        NotificationUtil.updateShutterNotification(context, ShutterService.FOREGROUND_NOTIFICATION_ID, 0, 1, addInfo);
    }

    private void eventMalfunction(String message) {
        eventMalfunction(message, null);
    }

    private void eventCameraShutdown() {
        Log.d(TAG, "eventCameraShutdown");
        if (Config.getPersistentCamera(context)) {

            switch (state) {
                case STATE_INIT: {
                    // camera failed during startup
                    restartCameraAfterMalfunction();
                    break;
                }
                case STATE_RESTART: {
                    try {
                        startCamera();
                    } catch (Exception e) {
                        eventMalfunction("restarting failed", e);
                        state = STATE_ERROR;
                        // do nothing
                    }
                    break;
                }
                case STATE_BUSY: {
                    // error occured while taking image
                    restartCameraAfterMalfunction();
                    return;
                }
                case STATE_SHUTDOWN: {
                    // onDestroy handles everything. do nothing
                    break;
                }
                case STATE_CAMERA_READY: {
                    eventMalfunction("something went wrong, camera was closed unexpectedly");
                    state = STATE_ERROR;
                    // do nothing
                    break;
                }
                case STATE_ERROR: {
                    // double error. do nothing
                    break;
                }
                default: {
                    eventMalfunction("invalid state in eventCameraShutdown: " + state);
                }
            }
        } else {
            handler.removeCallbacks(cameraWatchdogRunnable);
            Despat despat = Util.getDespat(this);
            despat.releaseWakeLock();
        }
    }


    private CameraController.ControllerCallback cameraCallback = new CameraController.ControllerCallback() {
        @Override
        public void cameraOpened() {
            Log.d(TAG, ":: cameraOpened");
        }

        @Override
        public void cameraReady(CameraController camera) {
            Log.d(TAG, ":: cameraReady");
            eventCameraReady();
        }

        @Override
        public void cameraFocused(CameraController camera, boolean afSuccessful) {
            Log.d(TAG, ":: cameraFocused (" + afSuccessful + ")");

            try {
                String suffix = null;
                if (state == STATE_SECOND_IMAGE || state == STATE_SECOND_IMAGE_BUSY) {
                    suffix = "_1";
                }
                camera.captureImages(suffix);
            } catch (Exception e) {
                eventMalfunction("capturing images failed", e);
            }
        }

        @Override
        public void intermediateImageTaken() {
            Log.d(TAG, ":: intermediateImageTaken");
        }

        @Override
        public void captureComplete(CaptureInfo info) {
            Log.d(TAG, ":: captureComplete");

            eventCaptureComplete(info);
        }

        @Override
        public void cameraClosed() {
            Log.d(TAG, ":: cameraClosed");
            eventCameraShutdown();
        }

        @Override
        public void cameraFailed(String message, Object o) {
            eventMalfunction("camera failed: + " + message, o);

            // camera should close itself and call onClosed
        }
    };

    // ---------------------------------------------------------------------------------------------

    private final BroadcastReceiver shutterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (state == STATE_SECOND_IMAGE) {
                triggerCamera();
                return;
            }

            Util.setHeartbeatManually(context, ShutterService.class);

            Despat despat = Util.getDespat(context);
            SystemController systemController = despat.getSystemController();
            SessionManager session = SessionManager.getInstance(context);
            float batteryLevel = systemController.getBatteryLevel();
            Log.i(TAG, "shutter released. BATT: " + batteryLevel + "% | IMAGES: " + session.getImagesTaken());

            if (batteryLevel <= Config.STOP_SESSION_AT_LOW_BATT_THRESHOLD) {
                Log.w(TAG, "battery level below threshold! recording session stop");
                Util.saveEvent(context, Event.EventType.LOW_BATTERY_STOP, "session stop due to low battery");

                try {
                    session.stopRecordingSession("low battery");
                } catch (SessionManager.NotRecordingException e) {
                    Log.e(TAG, "stopping session failed. attempting stop via broadcast", e);
                    Util.saveErrorEvent(context, "stopping session failed. attempting stop via broadcast", e);

                    Intent stopIntent = new Intent(context, Orchestrator.class);
                    stopIntent.putExtra(Orchestrator.SERVICE, Broadcast.SHUTTER_SERVICE);
                    stopIntent.putExtra(Orchestrator.OPERATION, Orchestrator.OPERATION_STOP);
                    stopIntent.putExtra(Orchestrator.REASON, "low battery (stop via broadcast)");
                    context.sendBroadcast(stopIntent);
                }

                // force sync so that servpat knows that the low-battery event occured
                if (Config.getPhoneHome(context)) {
                    Sync.run(context, ShutterService.class, true);
                }

                return;
            }

            if (Config.getPersistentCamera(context)) {

                long now = System.currentTimeMillis();
                long nextExecution = now + camconfig.getShutterInterval();
                nextExecution -= nextExecution % 1000;
                long delay = nextExecution - now;
                Log.d(TAG, "delay: " + delay);
                handler.postDelayed(shutterReleaseRunnable, delay);

                Intent nextInvocationIntent = new Intent(Broadcast.NEXT_SHUTTER_INVOCATION);
                nextInvocationIntent.putExtra(Broadcast.DATA_TIME, nextExecution);
                context.sendBroadcast(nextInvocationIntent);

                triggerCamera();

            } else {

                despat.acquireWakeLock(true);
                handler.postDelayed(cameraWatchdogRunnable, Config.SHUTTER_CAMERA_MAX_LIFETIME);

                try {
                    startCamera();
                } catch (Exception e) {
                    eventMalfunction("error during opening camera", e);

                    shutdownCamera();
                }
            }
        }
    };

    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED:
                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    Util.saveEvent(context, Event.EventType.SLEEP_MODE_CHANGE, "in idle mode: " + pm.isDeviceIdleMode());
                    Log.d(TAG, "+++ in idle mode: " + pm.isDeviceIdleMode());
                    break;

                case "android.intent.action.SCREEN_OFF":
                    Util.saveEvent(context, Event.EventType.DISPLAY_OFF, null);
                    Log.d(TAG, "+++ screen off");
                    break;

                case "android.intent.action.SCREEN_ON":
                    Util.saveEvent(context, Event.EventType.DISPLAY_ON, null);
                    Log.d(TAG, "+++ screen on");
                    break;

                default:
                    Log.e(TAG, "unknown intent: " + intent);
            }
        }
    };

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        state = STATE_SHUTDOWN;
        Despat despat = ((Despat) getApplicationContext());

        if (Config.getPersistentCamera(context)) {

            if (despat.getCamera() != null) {
                camera.closeCamera();
            } else {
                // camera already dead. something went wrong
                Log.w(TAG, "camera already dead on ShutterService shutdown");
            }

        } else {
            // is the camera still alive?
            if (despat.getCamera() != null && !despat.getCamera().isDead()) {
                Log.e(TAG, "camera still alive on ShutterService destroy");
                Util.saveEvent(despat, Event.EventType.ERROR, "camera still alive on ShutterService destroy");

                camera.closeCamera();
            }
        }

        try {
            unregisterReceiver(shutterReceiver);
        } catch (RuntimeException e) {
            Log.w(TAG, "unable to unregister receiver");
        }

        try {
            unregisterReceiver(powerReceiver);
        } catch (RuntimeException e) {
            Log.w(TAG, "unable to unregister receiver");
        }

        handler.removeCallbacksAndMessages(null);
        despat.releaseWakeLock();
        stopForeground(true);

        Log.d(TAG, "shutterService destroyed");
    }

}
