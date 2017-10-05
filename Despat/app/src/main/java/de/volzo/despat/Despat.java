package de.volzo.despat;

import android.app.Application;

import de.volzo.despat.support.CameraAdapter;
import de.volzo.despat.web.ServerConnector;

public class Despat extends Application {

    private CameraAdapter camera;
    private SystemController systemController;
    private int imagesTaken = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        systemController = new SystemController(this);

        // send APPSTART event
        ServerConnector serverConnector = new ServerConnector(this);
        serverConnector.sendEvent(ServerConnector.EventType.INIT, null);

    }

    public void setCamera(CameraAdapter cameraAdapter) {
        this.camera = cameraAdapter;
    }

    public CameraAdapter getCamera() {
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

    public void setImagesTaken(int imagesTaken) {
        this.imagesTaken = imagesTaken;
    }

    public int getImagesTaken() {
        return this.imagesTaken;
    }
}
