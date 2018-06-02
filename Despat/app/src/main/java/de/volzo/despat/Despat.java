package de.volzo.despat;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import org.acra.annotation.AcraCore;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.DevicePositioner;
import de.volzo.despat.support.ProximitySensor;
import de.volzo.despat.support.Util;

@AcraCore(buildConfigClass = BuildConfig.class)
public class Despat extends Application {

    public static String TAG = Despat.class.getSimpleName();

    Context context;

    private CameraController camera;
    private SystemController systemController;

    private PowerManager.WakeLock wakeLock;

    private ProximitySensor proximitySensor;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;

        Log.d(TAG, "despat Application Init");

        // Stetho Debug Library
        Stetho.initializeWithDefaults(this);

        if (Config.REDIRECT_LOGCAT) Util.redirectLogcat();

        systemController = new SystemController(this);

        try {
            Util.saveEvent(this, Event.EventType.INIT, null);
        } catch (IllegalStateException e) {
            Log.e(TAG, "room db schema outdated", e);
            Toast.makeText(context, "Database outdated", Toast.LENGTH_LONG).show();

            System.exit(0);
        }

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, Throwable arg1) {
                String name = "";
                String message = "";

                if (arg0 != null) name = arg0.getName();
                if (arg1 != null) message = arg1.getMessage();

                Log.e(TAG, "Global uncaught exception: " + name + " | " + message);

                if (arg1 != null) arg1.printStackTrace();

                Util.saveErrorEvent(context, "global uncaught exception in thread: " + name, arg1);
                Util.backupLogcat(null);

                // TODO: how to proceed? restart?
            }
        });

        initOrchestrator();

        proximitySensor = new ProximitySensor(this);
    }

    private void initOrchestrator() {
//        try {
//            unregisterReceiver();
//        }

        Log.d(TAG, "initializing broadcast receivers for Orchestrator");

        Orchestrator o = new Orchestrator();
        IntentFilter f = new IntentFilter();

        f.addAction(Broadcast.ALL_SERVICES);
        f.addAction(Broadcast.SHUTTER_SERVICE);
        f.addAction(Broadcast.RECOGNITION_SERVICE);
        f.addAction(Broadcast.HEARTBEAT_SERVICE);
        f.addAction(Broadcast.UPLOAD_SERVICE);
        f.addAction(Broadcast.COMMAND_SERVICE);
        f.addAction(Broadcast.PICTURE_TAKEN);
        f.addAction(Broadcast.ERROR_OCCURED);

        registerReceiver(o, f);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "despat terminate.");
        closeCamera();
        Util.saveEvent(this, Event.EventType.SHUTDOWN, null);
    }

    public PowerManager.WakeLock acquireWakeLock(boolean temporary) {
        if (temporary) {
            Log.d(TAG, "acquiring temporary wake lock");
        } else {
            Log.d(TAG, "acquiring permanent wake lock");
        }

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                Log.d(TAG, "wake lock is already held");
                return wakeLock;
            }
        } else {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DespatWakeLockTag");

            if (wakeLock == null) {
                Log.e(TAG, "acquiring wake lock failed");
                return null;
            }
        }

        Util.saveEvent(this, Event.EventType.WAKELOCK_ACQUIRE, null);

        if (temporary) {
            wakeLock.acquire(Config.WAKELOCK_MAX_LIFETIME);
        } else {
            wakeLock.acquire();
        }

        return wakeLock;
    }

    public void setWakeLock(PowerManager.WakeLock wakeLock) {
        this.wakeLock = wakeLock;
    }

    public void releaseWakeLock() {
        Log.d(TAG, "releasing wake lock");

        if (wakeLock == null) {
            Log.w(TAG, "wake lock missing");
        } else {
            if (wakeLock.isHeld()){
                wakeLock.release();

                Util.saveEvent(this, Event.EventType.WAKELOCK_RELEASE, null);
            } else{
                Log.d(TAG, "wake lock already released");
            }
        }
    }

    public void criticalErrorReboot() {
        Log.i(TAG, "CRITICAL ERROR REBOOT");

        if (Config.BACKUP_LOGCAT) Util.backupLogcat(null);

        SystemController systemController = getSystemController();
        Config.setResumeAfterReboot(this, true);
        systemController.reboot();
    }

    public CameraController initCamera(Context context) throws Exception {
        return initCamera(context, null, null);
    }

    public CameraController initCamera(Context context, CameraController.ControllerCallback controllerCallback, TextureView textureView) throws Exception {
        if (Config.getLegacyCameraController(context)) {
            Log.d(TAG, "initializing camera controller 1");
            this.camera = new CameraController1(context, controllerCallback, textureView);
        } else {
            Log.d(TAG, "initializing camera controller 2");
            this.camera = new CameraController2(context, controllerCallback, textureView);
        }

        return this.camera;
    }

    public CameraController getCamera() {
        return this.camera;
    }

    public void closeCamera() {
        if (camera != null) {
            camera.closeCamera();
            camera = null;
        }
    }

    public SystemController getSystemController() {
        return systemController;
    }

}
