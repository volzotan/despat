package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.02.18.
 */

public abstract class CameraController {

    public abstract void openCamera() throws Exception;
    public abstract void captureImages();
    public abstract void closeCamera();
    public abstract boolean isDead();

    public HashMap<String, String> getCameraParameters() {
        return null;
    }

    void sendBroadcast(Context context, String path) {
        Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
        if (path != null) intent.putExtra(Broadcast.DATA_PICTURE_PATH, path);
        context.sendBroadcast(intent);
    }

    SurfaceTexture getSurfaceTexture(TextureView tv) {
        if (tv != null) {
            return tv.getSurfaceTexture();
        } else {
            return new SurfaceTexture(ThreadLocalRandom.current().nextInt(1, 1000 + 1)); //0);
        }
    }

    public abstract static class ControllerCallback {

        public void cameraOpened() {}
        public void cameraClosed() {}
        public void cameraFailed(Object error) {}

        public void intermediateImageTaken() {}
        public void finalImageTaken() {}
        public void captureComplete() {}
    }

}