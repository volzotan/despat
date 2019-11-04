package de.volzo.despat;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import de.volzo.despat.persistence.Event;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.services.Orchestrator;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.DeviceInfo;
import de.volzo.despat.support.ProximitySensor;
import de.volzo.despat.support.Util;

//@AcraCore(buildConfigClass = BuildConfig.class)
public class Despat extends Application {

    public static String TAG = Despat.class.getSimpleName();

    Context context;

    private CameraController camera;
    private SystemController systemController;

    private PowerManager.WakeLock wakeLock;

    private StorageManager storageManager;
    private OnObbStateChangeListener obbListener;

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

//                Toast.makeText(context, "Global Error: " + name + " | " + message, Toast.LENGTH_LONG).show(); // not visible in different process
            }
        });

        initOrchestrator();
//        proximitySensor = new ProximitySensor(this);

        printSysinfo();

        try {
            mountObb();
        } catch (Exception e) {
            Log.e(TAG, "mounting OBB failed", e);
        }

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
        f.addAction(Broadcast.IMAGE_TAKEN);
        f.addAction(Broadcast.NEXT_SHUTTER_INVOCATION);
        f.addAction(Broadcast.ERROR_OCCURED);
        f.addAction(Broadcast.COMMAND_RUN_BENCHMARK);

        registerReceiver(o, f);
    }

    private void printSysinfo() {
        Log.i(TAG, new DeviceInfo(context).toString());
        Log.i(TAG, Config.print(context));
    }

    private void mountObb() throws Exception {

        if (storageManager == null) {
            storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        }

        String pathObb = Util.getObbPath(this);

        try {
            BufferedReader br = new BufferedReader(new FileReader(pathObb));
        } catch (IOException e) {
            Log.e(TAG, "missing write external storage permissions");
            throw e;
        }

        if (!(new File(pathObb)).exists()) {
            Log.e(TAG, "OBB file is missing: " + pathObb);
            Log.e(TAG, "Searched directories: ");
            for (File f : context.getObbDirs()) {
                Log.e(TAG, f.toString());
            }
            throw new Exception("missing OBB file: " + pathObb);

            // TODO: start download
        }

//        try {
//            ObbInfo info = ObbScanner.getObbInfo(pathObb);
//            Log.i(TAG, "obb filename: " + info.filename);
//            Log.i(TAG, "obb package name: " + info.packageName);
//            Log.i(TAG, "obb flags: " + Integer.toString(info.flags));
//            Log.i(TAG, "obb version: " + Integer.toString(info.version));
//        } catch (Exception e) {
//            Log.e(TAG, "scanning failed", e);
//        }

        if (storageManager.isObbMounted(pathObb)) {
            Log.i(TAG, "OBB already mounted at: " + storageManager.getMountedObbPath(pathObb));
            return;
        }

        if (obbListener == null) {
            Log.i(TAG, "obbListener created");
            createObbListener(pathObb);
        }

        storageManager.mountObb(pathObb, null, obbListener);
    }

    private void unmountObb() {

        if (storageManager == null) {
            Log.i(TAG, "no storage manager present. Unmounting OBBs unnecessary");
            return;
        }

        String pathObb = Util.getObbPath(this);

        if (obbListener == null) {
            Log.i(TAG, "obbListener created");
            createObbListener(pathObb);
        }

        storageManager.unmountObb(pathObb, true, obbListener);
    }

    private void createObbListener(final String pathObb) {
        obbListener = new OnObbStateChangeListener() {
            @Override
            public void onObbStateChange(String path, int state) {
                super.onObbStateChange(path, state);

                switch (state) {
                    case OnObbStateChangeListener.MOUNTED: {
                        Log.i(TAG, "OBB mounted: " + storageManager.getMountedObbPath(pathObb) + " | " + pathObb);
                        break;
                    }
                    case OnObbStateChangeListener.UNMOUNTED: {
                        Log.i(TAG, "OBB unmounted");
                        break;
                    }
                    case OnObbStateChangeListener.ERROR_ALREADY_MOUNTED: {
                        Log.e(TAG, "mounting failed: already mounted");
                        break;
                    }
                    case OnObbStateChangeListener.ERROR_COULD_NOT_MOUNT: {
                        Log.e(TAG, "mounting failed: could not mount");
                        break;
                    }
                    case OnObbStateChangeListener.ERROR_INTERNAL: {
                        Log.e(TAG, "mounting failed: error internal");
                        break;
                    }
                    case OnObbStateChangeListener.ERROR_PERMISSION_DENIED: {
                        Log.e(TAG, "mounting failed: error permission denied");
                        break;
                    }
                    default: {
                        Log.wtf(TAG, "path: " + path + " " + Integer.toString(state));
                    }
                }
            }
        };

    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "despat terminate.");
        closeCamera();
        unmountObb();
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
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "de.volzo.despat:despatWakeLockTag");

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
        return initCamera(context, null, null, new CameraConfig(this));
    }

    public CameraController initCamera(Context context, CameraController.ControllerCallback controllerCallback, TextureView textureView, CameraConfig cameraConfig) throws Exception {
        if (cameraConfig == null) {
            throw new NullPointerException("camera config missing");
        }

        if (Config.getLegacyCameraController(context)) {
            Log.d(TAG, "initializing camera controller 1");
            this.camera = new CameraController1(context, controllerCallback, textureView);
        } else {
            Log.d(TAG, "initializing camera controller 2");
            this.camera = new CameraController2(context, controllerCallback, textureView, cameraConfig);
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
