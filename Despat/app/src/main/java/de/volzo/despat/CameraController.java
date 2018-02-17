package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.02.18.
 */

public abstract class CameraController {

    public static final String TAG = CameraController.class.getSimpleName();

    public CameraController.ControllerCallback callback;

    public abstract void openCamera() throws Exception;
    public abstract void captureImages() throws IllegalAccessException;
    public abstract void closeCamera();
    public abstract boolean isDead();

    protected void cameraFailed(String message, Object o) {
        if (o instanceof Exception) {
            Log.e(TAG, "camera failed: " + message, (Exception) o);
        } else if (o != null) {
            Log.e(TAG, "camera failed: " + message + " [" + o.toString() + "]");
        } else {
            Log.e(TAG, "camera failed: " + message);
        }

        if (callback != null) callback.cameraFailed(message, o);
        closeCamera();
    }

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
        public void cameraFailed(String message, Object error) {}

        public void intermediateImageTaken() {}
        public void finalImageTaken() {}
        public void captureComplete() {}
    }

}