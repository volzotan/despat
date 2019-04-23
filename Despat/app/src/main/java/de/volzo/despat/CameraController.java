package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Broadcast;

/**
 * Created by volzotan on 04.02.18.
 */

public abstract class CameraController {

    private static final String TAG = CameraController.class.getSimpleName();

    public CameraController.ControllerCallback callback;

    public abstract void openCamera() throws Exception;
    public abstract void closeCamera();

    public abstract void startMetering(Integer optionalExposureCompensation) throws Exception;
    public abstract void captureImages(String filenameSuffix) throws Exception;

    public abstract boolean isDead();

    protected void reportFailAndClose(String message, Object o) {
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

    void sendBroadcast(Context context, CaptureInfo info) {
        Intent intent = new Intent(Broadcast.IMAGE_TAKEN);
        if (info != null) intent.putExtra(Broadcast.DATA_IMAGE_CAPTUREINFO, info);
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

        // camera opened and present
        public void cameraOpened() {}

        // additional preparation after opening has finished
        public void cameraReady(CameraController camera) {}

        // preview is present (visible or invisible) and autofocus (and probably metering) runs
        // have finished
        public void cameraFocused(CameraController camera, boolean afSuccessful) {}

        // camera has finished closing and all resources have been closed
        public void cameraClosed() {}

        // error happened, camera will close itself and call cameraClosed() next
        public void cameraFailed(String message, Object error) {}

        // called in burst mode after pictures 0 : n-1
        public void intermediateImageTaken() {}

        // called after completing the capture (in burst mode after the last image)
        // the image is not guaranteed to be written to disk at calling time
        // the CaptureInfo object represents the last image in burst mode
        public void captureComplete(CaptureInfo info) {}

    }
}