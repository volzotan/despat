package de.volzo.despat;

import android.app.Application;
import android.os.PowerManager;
import android.util.Log;

import de.volzo.despat.web.ServerConnector;

public class Despat extends Application {

    public static String TAG = Despat.class.getSimpleName();

    private CameraController camera;
    private SystemController systemController;
//    private int imagesTaken = 0;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        systemController = new SystemController(this);

        // send APPSTART event
        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendEvent(ServerConnector.EventType.INIT, null);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.i(TAG, "despat terminate.");

        closeCamera();
        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendEvent(ServerConnector.EventType.SHUTDOWN, null);
    }

    public void acquireWakeLock() {
        Log.d(TAG, "acquiring wake lock");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DespatWakeLockTag");
        wakeLock.acquire();
    }

    public void releaseWakeLock() {
        Log.d(TAG, "releasing wake lock");

        if (wakeLock == null) {
            Log.w(TAG, "wake lock missing");
        } else {
            if (wakeLock.isHeld()){
                wakeLock.release();
            } else{
                // do nothing
            }
        }
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
