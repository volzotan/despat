package de.volzo.despat;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;
import de.volzo.despat.web.ServerConnector;

@AcraCore(buildConfigClass = BuildConfig.class)
public class Despat extends Application {

    public static String TAG = Despat.class.getSimpleName();

    private CameraController camera;
    private SystemController systemController;
//    private int imagesTaken = 0;

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

        systemController = new SystemController(this);

        // send APPSTART event
//        ServerConnector serverConnector = new ServerConnector(this);
//        serverConnector.sendEvent(ServerConnector.EventType.INIT, null);

        Util.saveEvent(this, Event.EventType.INIT, null);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "despat terminate.");

        closeCamera();

//        ServerConnector serverConnector = new ServerConnector(this);
//        serverConnector.sendEvent(ServerConnector.EventType.SHUTDOWN, null);

        Util.saveEvent(this, Event.EventType.SHUTDOWN, null);
    }

    public void acquireWakeLock() {
        Log.d(TAG, "acquiring wake lock");

        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                Log.d(TAG, "wake lock is already held");
                return;
            }
        } else {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DespatWakeLockTag");
        }

        wakeLock.acquire(Config.WAKELOCK_MAX_LIFETIME);
    }

    public void releaseWakeLock() {
        Log.d(TAG, "releasing wake lock");

        if (wakeLock == null) {
            Log.w(TAG, "wake lock missing");
        } else {
            if (wakeLock.isHeld()){
                wakeLock.release();
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

    public void setCamera(CameraController cameraController) {
        this.camera = cameraController;
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
