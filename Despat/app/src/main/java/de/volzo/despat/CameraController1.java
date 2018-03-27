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
import java.util.HashMap;
import java.util.List;

import de.volzo.despat.persistence.Event;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 04.02.18.
 */

public class CameraController1 extends CameraController implements Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback, Camera.AutoFocusCallback, Camera.AutoFocusMoveCallback, Camera.ErrorCallback {

    public static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private CameraController.ControllerCallback controllerCallback;

    private CameraController1 controller;

    private Camera camera;
    private Camera.Parameters params;
    private int[] pictureSize;

    private int shutterCount = 0;

    Handler handler;
    Runnable releaseCall = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "shutter release by timeout");
            camera.cancelAutoFocus();
            controller.releaseShutter();
        }
    };

    public CameraController1(Context context, ControllerCallback controllerCallback, TextureView textureView) throws Exception {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;

        this.controller = this;
    }

    public void openCamera() throws Exception {
        Log.d(TAG, "# openCamera");

        openCameraInternal();

        if (textureView == null) {
            captureImages();
        } else {
            camera.startPreview();
        }

        if (controllerCallback != null) {
            controllerCallback.cameraOpened();
        }
    }

    private void openCameraInternal() throws Exception {
        Log.d(TAG, "# openCameraInternal");

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
            // Log.v(TAG, "camera preview format: " + w + "x" + h);
            if (w > max_width || h > max_height) {
                max_width = w;
                max_height = h;
            }
        }
        params.setPictureSize(max_width, max_height);
        pictureSize = new int[] {params.getPictureSize().width, params.getPictureSize().height};
        Log.d(TAG, "supported picture size: " + pictureSize[0] + " x " + pictureSize[1]);

        // AF
        List<String> modes = params.getSupportedFocusModes();
        if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            Log.d(TAG, "focus mode: infinity");
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            Log.d(TAG, "focus mode: auto");
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else {
            Log.w(TAG, "focus mode: none (neither infinity focus mode nor auto focus mode available)");
        }

//        params.setPictureFormat();

        camera.setParameters(params);
        camera.setPreviewTexture(getSurfaceTexture(textureView));
    }

    @Override
    public void startMetering() {
        // TODO
    }

    @Override
    public void captureImages() throws IllegalAccessException {
        Log.d(TAG, "# captureImages");

        if (camera == null) {
            throw new IllegalAccessException("camera is dead");
        }

        try {
            precapture(0);
        } catch (Exception e) {
            reportFailAndClose("capturing image failed", e);
        }
    }

    private void precapture(int sequenceNumber) throws Exception {
        Log.d(TAG, "# precapture [" + (sequenceNumber+1) + "/" + Config.NUMBER_OF_BURST_IMAGES + "]");

        if (camera == null) {
            Log.e(TAG, "camera died unexpectedly");
            throw new IllegalAccessException("camera died unexpectedly");
        }

        Util.sleep(200);

        try {
            camera.startPreview();
        } catch (RuntimeException re) {
            Log.w(TAG, "camera failed during preview. attempting restart.");
            try {
                restartCamera();
                camera.startPreview();
                Util.saveEvent(context, Event.EventType.ERROR, "preview failed. camera restart");
            } catch (Exception e) {
                Log.e(TAG, "preview failed. restarting camera failed", e);
                throw e;
            }
        }

        // AE - exposure compensation
        boolean ae_measurement_required = false;
        try {
            if (Config.EXPOSURE_COMPENSATION.length > 1) {
                params.setExposureCompensation(Config.EXPOSURE_COMPENSATION[sequenceNumber]);
                if (sequenceNumber > 0 && Config.EXPOSURE_COMPENSATION[sequenceNumber-1] != Config.EXPOSURE_COMPENSATION[sequenceNumber]) {
                    ae_measurement_required = true;
                }
            } else {
                params.setExposureCompensation(Config.EXPOSURE_COMPENSATION[0]);
            }
            camera.setParameters(params);
        } catch (RuntimeException re) {
            Log.w(TAG, "setting exposure compensation params failed. value too higher/lower than maxExposureCompensation?", re);
        }

        // AE - iso
        try {
            if (Config.FIXED_ISO_VALUE != null) {
                params.set("iso-value", Config.FIXED_ISO_VALUE);
                camera.setParameters(params);
            }
        } catch (RuntimeException re) {
            Log.w(TAG, "setting iso params failed. probably not supported", re);
        }

        // auto focus only on first image
        if (sequenceNumber == 0) {

            handler = new Handler();
            if (params.getFocusMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                camera.autoFocus(this);
            }

            switch (params.getFocusMode()) {
                case Camera.Parameters.FLASH_MODE_AUTO:
                    handler.postDelayed(releaseCall, Config.METERING_MAX_TIME);
                    camera.autoFocus(this);
                    break;

                case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE:
                    handler.postDelayed(releaseCall, Config.METERING_MAX_TIME);
                    camera.autoFocus(this);
                    break;

                case Camera.Parameters.FOCUS_MODE_INFINITY:
                    Util.sleep(Config.SHUTTER_RELEASE_DELAY);
                    controller.releaseShutter();
                    break;

                default:
                    Log.w(TAG, "unknown auto focus mode! [" + params.getFocusMode() + "] releasing shutter anyway.");
                    controller.releaseShutter();
                    break;
            }
        } else {
            if (ae_measurement_required) Util.sleep(Config.SHUTTER_RELEASE_DELAY);
            controller.releaseShutter();
        }
    }

    private void releaseShutter() {
        try {
            camera.takePicture(controller, controller, controller);
        } catch (RuntimeException re) {
            Log.e(TAG, "releasing shutter failed: ", re);
            reportFailAndClose("releasing shutter failed", re);
        }
    }

    @Override
    public void closeCamera() {
        Log.d(TAG, "# closeCamera");

        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        } else {
            Log.d(TAG, "camera closing failed! camera is null");
        }

        if (controllerCallback != null) {
            controllerCallback.cameraClosed();
        }
    }

    private void restartCamera() throws Exception {
        Log.i(TAG, "# restartCamera");

        if (textureView != null) {
            throw new Exception("no restart if previewing");
        }

        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        openCameraInternal();
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        Log.d(TAG, "# onPictureTaken");

        if (bytes == null) {
            // this callback is called once with raw image data and once with processed jpeg data
            // if the current phone doesn't support raw data, bytes will be empty
            Log.d(TAG, "image data empty");
            return;
        }

        Log.d( TAG, "imageCallback: picture retrieved ("+bytes.length+" bytes)" );

        final ImageRollover imgroll = new ImageRollover(context, ".jpg");
        File imageFullPath = imgroll.getTimestampAsFullFilename();
        if (imageFullPath == null) { // only duplicates
            Log.e(TAG, "saving image failed. no new filename could be acquired");
            return;
        }

        Camera.Parameters p = camera.getParameters();

        if (p.getPictureFormat() == ImageFormat.JPEG) {
            // it's already JPEG
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                fos.write(bytes);
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "saving JPEG failed ", e);
                return;
            }
        } else if ( p.getPictureFormat() == ImageFormat.YUV_420_888 ||
                    p.getPictureFormat() == ImageFormat.YUV_422_888 ||
                    p.getPictureFormat() == ImageFormat.YUV_444_888 ||
                    p.getPictureFormat() == ImageFormat.YUY2 ||
                    p.getPictureFormat() == ImageFormat.YV12) {

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
        } else {
            Log.wtf(TAG, "RAW");
        }

        shutterCount++;

        Log.d(TAG, "picture taken [" + shutterCount + "/" + Config.NUMBER_OF_BURST_IMAGES + "]");

        if (shutterCount < Config.NUMBER_OF_BURST_IMAGES) {
            if (controllerCallback != null) {
                controllerCallback.intermediateImageTaken();
            }

            try {
                precapture(shutterCount);
            } catch (Exception e) {
                reportFailAndClose("capturing next image in sequence failed", e);
            }
        } else {
            Log.d(TAG, "# captureComplete");

            sendBroadcast(context, imageFullPath.getAbsolutePath());

            if (controllerCallback != null) {
                controllerCallback.captureComplete();
            }

            shutterCount = 0;
        }

        if (textureView != null) {
            // TODO: restart auto focus? / disable autofocus? should be done if continuous
            camera.startPreview();
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
        handler.removeCallbacksAndMessages(null);
        camera.cancelAutoFocus();

        if (success) {
            Log.d(TAG, "shutter release by autofocus success");
        } else {
            Log.d(TAG, "shutter release by autofocus failure");
        }

        releaseShutter();
    }

    @Override
    public void onAutoFocusMoving(boolean start, Camera camera) {
        Log.d(TAG, "# onAutoFocusMoving");
    }

    @Override
    public void onError(int error, Camera camera) {
        Log.d(TAG, "# onError");
        reportFailAndClose("onError called", error);
    }

    @Override
    public boolean isDead() {
        if (camera == null) {
            return true;
        } else {
            return false;
        }
    }

    public HashMap<String, String> getCameraParameters() {
        Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();

        String flat = params.flatten();

        HashMap<String, String> dict = new HashMap<String, String>();
        for (String s : flat.split(";")) {
            String[] e = s.split("=");
            if (e.length == 2) {
                dict.put(e[0], e[1]);
            } else {
                dict.put(e[0], "");
            }
        }

        camera.release();
        return dict;
    }

}
