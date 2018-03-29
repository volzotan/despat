package de.volzo.despat;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.TextureView;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;

import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

@AcraCore(buildConfigClass = BuildConfig.class)
public class Despat extends Application {

    public static String TAG = Despat.class.getSimpleName();

    private CameraController camera;
    private SystemController systemController;

    private PowerManager.WakeLock wakeLock;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Stetho Debug Library
        Stetho.initializeWithDefaults(this);

        if (Config.REDIRECT_LOGCAT) Util.redirectLogcat();

        systemController = new SystemController(this);

        Util.saveEvent(this, Event.EventType.INIT, null);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "despat terminate.");
        closeCamera();
        Util.saveEvent(this, Event.EventType.SHUTDOWN, null);
    }

    public void acquireWakeLock(boolean temporary) {
        if (temporary) {
            Log.d(TAG, "acquiring temporary wake lock");
        } else {
            Log.d(TAG, "acquiring permanent wake lock");
        }

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                Log.d(TAG, "wake lock is already held");
                return;
            }
        } else {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DespatWakeLockTag");

            if (wakeLock == null) {
                Log.e(TAG, "acquiring wake lock failed");
                return;
            }
        }

        Util.saveEvent(this, Event.EventType.WAKELOCK_ACQUIRE, null);

        if (temporary) {
            wakeLock.acquire(Config.WAKELOCK_MAX_LIFETIME);
        } else {
            wakeLock.acquire();
        }
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
        switch (Config.USE_CAMERA_CONTROLLER) {
            case 1:
                this.camera = new CameraController1(context, controllerCallback, textureView);
                break;
            case 2:
                this.camera = new CameraController2(context, controllerCallback, textureView);
                break;
//            case 3:
//                this.camera = new CameraController3(context, controllerCallback, textureView);
//                break;
            default:
                throw new Exception("unknown camera controller");
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
