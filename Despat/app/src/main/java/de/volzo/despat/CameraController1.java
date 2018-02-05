package de.volzo.despat;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.volzo.despat.support.Config;

/**
 * Created by volzotan on 04.02.18.
 */

public class CameraController1 extends CameraController implements Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback, Camera.AutoFocusCallback, Camera.AutoFocusMoveCallback, Camera.ErrorCallback {

    public static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private CameraController.ControllerCallback controllerCallback;

    Camera camera;
    private Camera.Parameters param;
    private int[] pictureSize;

    public CameraController1(Context context, ControllerCallback controllerCallback, TextureView textureView) throws Exception {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;

        try {;
            openCamera();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
            throw e;
        }
    }

    private void openCamera() throws java.io.IOException {
        camera = Camera.open();

        camera.setAutoFocusMoveCallback(this);
        camera.setErrorCallback(this);

        param = camera.getParameters();
        List<Camera.Size> pvsizes = param.getSupportedPreviewSizes();

        pictureSize = new int[] {param.getPictureSize().width, param.getPictureSize().height};
        Log.d(TAG, "supported picture size: " + pictureSize[0] + " x " + pictureSize[1]);

        int len = pvsizes.size();
        for (int i = 0; i < len; i++)
            Log.v( TAG, "camera preview format: "+pvsizes.get(i).width+"x"+pvsizes.get(i).height );

        param.setPreviewFormat( ImageFormat.NV21 );
        //param.setPreviewSize( pvsizes.get(len-1).width, pvsizes.get(len-1).height );
        param.setPreviewSize(1280, 960);

        camera.setParameters(param);
        camera.setPreviewTexture(getSurfaceTexture(textureView));
        // camera.setDisplayOrientation(90);

        if (textureView == null) {
            captureImages();
        } else {
            camera.startPreview();
        }

        if (controllerCallback != null) {
            controllerCallback.cameraOpened();
        }
    }

    @Override
    public void captureImages() {
        // camera.setOneShotPreviewCallback(this);

        camera.startPreview();
        try {
            Thread.sleep(1000); // TODO
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.takePicture(this, this, this);
    }

    @Override
    public void closeCamera() {
        camera.stopPreview();
        camera.release();

        if (controllerCallback != null) {
            controllerCallback.cameraClosed();
        }
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {

        if (bytes == null) {
            // this callback is called once with raw image data and once with processed jpeg data
            // if the current phone doesn't support raw data, bytes will be empty
            Log.d(TAG, "image data empty");
            return;
        }

        Log.d( TAG, "imageCallback: picture retrieved ("+bytes.length+" bytes)" );

        final ImageRollover imgroll = new ImageRollover(context);
        File imageFullPath = imgroll.getTimestampAsFullFilename();
        if (imageFullPath == null) { // only duplicates
            Log.e(TAG, "saving image failed. no new filename could be acquired");
            return;
        }

        if (param.getPictureFormat() == ImageFormat.JPEG) {
            // it's already JPEG
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                fos.write(bytes);
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "saving JPEG failed ", e);
                return;
            }
        } else {
            // try to store YUV data
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                Log.d(TAG, "image format: " + param.getPictureFormat());
                YuvImage image = new YuvImage(bytes, param.getPictureFormat(), pictureSize[0], pictureSize[1], null);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);

                // fos.close();

            } catch (Exception e) {
                Log.e(TAG, "saving YUV failed ", e);
                return;
            }
        }

        try {
            Log.d(TAG, "picture stored:  " + imageFullPath.getCanonicalPath());

            sendBroadcast(context, imageFullPath.getAbsolutePath());

            if (controllerCallback != null) {
                controllerCallback.finalImageTaken();
            }
            if (controllerCallback != null) {
                controllerCallback.captureComplete();
            }

//            if (textureView == null) {
//                this.closeCamera();
//            }
        } catch (IOException e) {
            Log.e(TAG, "accessing image path failed ", e);
            return;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.d(TAG, "# onPreviewFrame");
    }

    @Override
    public void onShutter() {
        Log.d(TAG, "# onShutter");
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.d(TAG, "# onAutoFocus");

    }

    @Override
    public void onAutoFocusMoving(boolean start, Camera camera) {
        Log.d(TAG, "# onAutoFocusMoving");

    }

    @Override
    public void onError(int error, Camera camera) {
        Log.d(TAG, "# onError");

        if (controllerCallback != null) {
            controllerCallback.cameraFailed(error);
        }

        closeCamera();
    }

    @Override
    public boolean isDead() {
        if (camera == null) {
            return true;
        } else {
            return false;
        }
    }

}
