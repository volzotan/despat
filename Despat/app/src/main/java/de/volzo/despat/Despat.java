package de.volzo.despat;

import android.app.Application;

import de.volzo.despat.support.CameraAdapter;

public class Despat extends Application {

    private CameraAdapter camera;
    private int imagesTaken = 0;

    @Override
    public void onCreate() {
        super.onCreate();

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

    public void setImagesTaken(int imagesTaken) {
        this.imagesTaken = imagesTaken;
    }

    public int getImagesTaken() {
        return this.imagesTaken;
    }
}
