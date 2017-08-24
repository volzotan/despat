package de.volzo.despat;

import android.app.Application;

import de.volzo.despat.support.CameraAdapter;

public class Despat extends Application {

    private CameraAdapter camera;

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
}
