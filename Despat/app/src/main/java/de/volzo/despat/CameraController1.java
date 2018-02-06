package de.volzo.despat;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
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

    private CameraController1 controller;

    Camera camera;
    private Camera.Parameters params;
    private int[] pictureSize;

    private int shutterCount = 0;

    public CameraController1(Context context, ControllerCallback controllerCallback, TextureView textureView) throws Exception {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;

        this.controller = this;

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

        params = camera.getParameters();

        // Preview Size
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        params.setPreviewFormat( ImageFormat.NV21 );
        //param.setPreviewSize( previewSizes.get(len-1).width, previewSizes.get(len-1).height );
        params.setPreviewSize(1280, 960); // TODO: hardcoded

        // Picture Size
        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        int max_width = 0;
        int max_height = 0;
        for (int i = 0; i < pictureSizes.size(); i++) {
            int w = pictureSizes.get(i).width; int h = pictureSizes.get(i).height;
//            Log.v(TAG, "camera preview format: " + w + "x" + h);
            if (w > max_width || h > max_height) {
                max_width = w;
                max_height = h;
            }
        }
        params.setPictureSize(max_width, max_height);
        pictureSize = new int[] {params.getPictureSize().width, params.getPictureSize().height};
        Log.d(TAG, "supported picture size: " + pictureSize[0] + " x " + pictureSize[1]);

        // AF/AE
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setExposureCompensation(Config.EXPOSURE_COMPENSATION);

        // Log
        Log.d(TAG, "param: exposureCompensation: " + params.getExposureCompensation());
        Log.d(TAG, "param: exposureCompensationStep: " + params.getExposureCompensationStep());
        Log.d(TAG, "param: minExposureCompensation: " + params.getMinExposureCompensation());
        Log.d(TAG, "param: maxExposureCompensation: " + params.getMaxExposureCompensation());
        Log.d(TAG, "param: whiteBalance: " + params.getWhiteBalance());
        Log.d(TAG, "param: focusMode: " + params.getFocusMode());
        Log.d(TAG, "param: focalLength: " + params.getFocalLength());
        Log.d(TAG, "param: zoomSupported: " + params.isZoomSupported());
        Log.d(TAG, "param: pictureFormat: " + params.getPictureFormat());
        Log.d(TAG, "param: pictureSize: " + params.getPictureSize().width + "x" + params.getPictureSize().height);

        camera.setParameters(params);
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

        shutterCount = 0;
    }

    @Override
    public void captureImages() {
        // camera.setOneShotPreviewCallback(this);

        camera.startPreview();
        try {
            Thread.sleep(500); // TODO: AF/AE adjust
        } catch (Exception e) {
            e.printStackTrace();
        }

        camera.takePicture(controller, controller, controller);
    }

    @Override
    public void closeCamera() {
        Log.d(TAG, "# closeCamera");

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

        if (params.getPictureFormat() == ImageFormat.JPEG) {
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
            Log.i(TAG, "image in YUV format");
            // try to store YUV data
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                Log.d(TAG, "image format: " + params.getPictureFormat());
                YuvImage image = new YuvImage(bytes, params.getPictureFormat(), pictureSize[0], pictureSize[1], null);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);

                // fos.close();

            } catch (Exception e) {
                Log.e(TAG, "saving YUV failed ", e);
                return;
            }
        }

        shutterCount++;

        Log.d(TAG, "picture taken [" + shutterCount + "/" + Config.NUMBER_OF_BURST_IMAGES + "]");
        sendBroadcast(context, imageFullPath.getAbsolutePath());

        if (shutterCount < Config.NUMBER_OF_BURST_IMAGES) {
            if (controllerCallback != null) {
                controllerCallback.intermediateImageTaken();
            }

            camera.startPreview();
            camera.takePicture(controller, controller, controller);
        } else {
            Log.d(TAG, "# captureComplete");
            if (controllerCallback != null) {
                controllerCallback.finalImageTaken();
            }
            if (controllerCallback != null) {
                controllerCallback.captureComplete();
            }

            shutterCount = 0;
        }

//            if (textureView == null) {
//                this.closeCamera();
//            }

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
