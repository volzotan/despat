package de.volzo.despat;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

import de.volzo.despat.persistence.Event;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.support.Util;

/**
 * Created by volzotan on 04.02.18.
 */

public class CameraController1 extends CameraController implements Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback, Camera.AutoFocusCallback, Camera.AutoFocusMoveCallback, Camera.ErrorCallback {

    private static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private CameraController.ControllerCallback controllerCallback;

    private CameraController1 controller;

    private Camera camera;
    private Camera.Parameters params;
    private int[] pictureSize;

    private int shutterCount = 0;
    private long captureTimer;

    private Boolean autoFocusResult = null;
    private Boolean autoExposureResult = null;

    private Handler handler;

    private Runnable autoFocusStopCall = new Runnable() {
        @Override
        public void run() {

            if (autoFocusResult != null) {
                // autofocus callback was successful but AE metering hasn't ended yet
                return;
            }

            if (camera == null) {
                Log.w(TAG, "camera killed without cancelling delayed task");
                return;
            }

            Log.d(TAG, "AF end by timeout");
            camera.cancelAutoFocus();
            autoFocusResult = false;

            if (autoFocusResult != null && autoExposureResult != null) {
                meteringFinished(autoFocusResult);
            }
        }
    };

    private Runnable autoExposureStopCall = new Runnable() {
        @Override
        public void run() {

            Log.d(TAG, "AE finished");
            autoExposureResult = true;

            if (autoFocusResult != null && autoExposureResult != null) {
                meteringFinished(autoFocusResult);
            }
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

        if (controllerCallback != null) {
            controllerCallback.cameraOpened();
            controllerCallback.cameraReady(controller);
        }

        if (textureView != null) {
            camera.startPreview();
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.wtf(TAG, "camera is running on UI thread");
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
        if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            Log.d(TAG, "focus mode: auto");
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            Log.d(TAG, "focus mode: infinity");
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            Log.w(TAG, "focus mode: none (neither infinity focus mode nor auto focus mode available)");
        }

//        params.setPictureFormat();

        camera.setParameters(params);
        camera.setPreviewTexture(getSurfaceTexture(textureView));
    }

    @Override
    public void startMetering(Integer optionalExposureCompensation) {
        try {
            precapture(true, true, optionalExposureCompensation);
        } catch (Exception e) {
            reportFailAndClose("metering failed", e);
        }
    }

    @Override
    public void captureImages(String filenameSuffix) throws Exception {
        Log.d(TAG, "# captureImages");

        if (camera == null) {
            throw new IllegalAccessException("camera is dead");
        }

        shutterCount = 0;
        releaseShutter(); // TODO: use filenameSuffix

//        for (int i=0; i<Config.NUMBER_OF_BURST_IMAGES; i++) { // TODO
//            try {
//                camera.takePicture(controller, controller, controller);
//            } catch (RuntimeException re) {
//                reportFailAndClose("releasing shutter failed [" + (i+1) + "/" + Config.NUMBER_OF_BURST_IMAGES + "]", re);
//            }
//        }

//        try {
//            for (int i=0; i<Config.NUMBER_OF_BURST_IMAGES; i++) {
//                if (i == 0) {
//
//                    // for the first image AF and AE are required
//
//                    precapture(true, true, Config.EXPOSURE_COMPENSATION[0]);
//                    releaseShutter();
//                } else {
//
//                    // for every subsequent image, AF should be fine and AE is only required
//                    // if different exposure compensation values are used
//
//                    if (Config.EXPOSURE_COMPENSATION[i-1] != Config.EXPOSURE_COMPENSATION[i]) {
//                        precapture(false, true, Config.EXPOSURE_COMPENSATION[i]);
//                    }
//
//                    releaseShutter();
//                }
//            }
//        } catch (Exception e) {
//            reportFailAndClose("capturing image failed", e);
//        }
    }

    private void precapture(boolean runAF, boolean runAE, Integer optionalExposureCompensation) throws Exception {
        Log.d(TAG, "# precapture");

        autoFocusResult = null;
        autoExposureResult = null;

        if (camera == null) {
            Log.e(TAG, "camera died unexpectedly");
            throw new IllegalAccessException("camera died unexpectedly");
        }

        Util.sleep(200); // TODO: ?

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
        try {
            if (optionalExposureCompensation != null) {
                params.setExposureCompensation(optionalExposureCompensation);
            } else {
                params.setExposureCompensation(Config.getExposureCompensation(context));
            }
            camera.setParameters(params);
        } catch (RuntimeException re) {
            Log.w(TAG, "setting exposure compensation params failed. value higher/lower than maxExposureCompensation?", re);
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

        handler = new Handler();

        if (runAF) {
            switch (params.getFocusMode()) {
                case Camera.Parameters.FLASH_MODE_AUTO:
                    handler.postDelayed(autoFocusStopCall, Config.METERING_MAX_TIME);
                    camera.autoFocus(this);
                    break;

                case Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE:
                    handler.postDelayed(autoFocusStopCall, Config.METERING_MAX_TIME);
                    camera.autoFocus(this);
                    break;

                case Camera.Parameters.FOCUS_MODE_INFINITY:
                    autoFocusResult = false;
                    break;

                default:
                    Log.w(TAG, "unknown auto focus mode! [" + params.getFocusMode() + "] AF unsuccessful.");
                    autoFocusResult = false;
                    break;
            }
        } else {
            autoFocusResult = false;
        }

        if (runAE) {
            handler.postDelayed(autoExposureStopCall, Config.SHUTTER_RELEASE_DELAY);
        } else {
            autoExposureResult = false;
        }

        if (autoFocusResult != null && autoExposureResult != null) {
            if (controllerCallback != null) controllerCallback.cameraFocused(controller, autoFocusResult);
        } else {
            captureTimer = SystemClock.elapsedRealtime();
        }

    }

    private void releaseShutter() {
        try {
            camera.takePicture(controller, controller, controller);
        } catch (RuntimeException re) {
            reportFailAndClose("releasing shutter failed", re);
        }
    }

    @Override
    public void closeCamera() {
        Log.d(TAG, "# closeCamera");

        handler.removeCallbacks(null);

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

        // this callback may be called once with raw image data and once with processed jpeg data
        // if the current phone doesn't support raw data, bytes will be empty or only
        // one call will happen

        if (bytes == null) {
            Log.d(TAG, "image data empty");
            return;
        }

        Log.d( TAG, "imageCallback: picture retrieved (" + bytes.length/1024 + "kb)" );

        ImageRollover imgroll = new ImageRollover(context, ".jpg");
        final File imageFullPath = imgroll.getTimestampAsFullFilename();
        if (imageFullPath == null) { // only duplicates
            Log.e(TAG, "saving image failed. no new filename could be acquired");
            return;
        }

        Camera.Parameters p = camera.getParameters();
        final int pictureFormat = p.getPictureFormat();
        final byte[] imagedata = bytes;

        Runnable saver = new Runnable() {
            @Override
            public void run() {
                if (pictureFormat == ImageFormat.JPEG) {
                    // it's already JPEG
                    try {
                        FileOutputStream fos = new FileOutputStream(imageFullPath);
                        fos.write(imagedata);
                        fos.close();
                    } catch (Exception e) {
                        Log.e(TAG, "saving JPEG failed ", e);
                        return;
                    }
                } else if ( pictureFormat == ImageFormat.YUV_420_888 ||
                            pictureFormat == ImageFormat.YUV_422_888 ||
                            pictureFormat == ImageFormat.YUV_444_888 ||
                            pictureFormat == ImageFormat.YUY2 ||
                            pictureFormat == ImageFormat.YV12) {

                    Log.i(TAG, "image in YUV format");

                    // try to store YUV data
                    try {
                        FileOutputStream fos = new FileOutputStream(imageFullPath);
                        Log.d(TAG, "image format: " + params.getPictureFormat());
                        YuvImage image = new YuvImage(imagedata, params.getPictureFormat(), pictureSize[0], pictureSize[1], null);
                        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);

                        // fos.close();

                    } catch (Exception e) {
                        Log.e(TAG, "saving YUV failed ", e);
                        return;
                    }
                } else {
                    Log.wtf(TAG, "RAW");
                }
            }
        };
        AsyncTask.THREAD_POOL_EXECUTOR.execute(saver);

        shutterCount++;

        Log.d(TAG, "picture taken [" + shutterCount + "/" + Config.NUMBER_OF_BURST_IMAGES + "]");

        if (shutterCount < Config.NUMBER_OF_BURST_IMAGES) {
            if (controllerCallback != null) {
                controllerCallback.intermediateImageTaken();
            }

            // preview needs to be restarted before another picture can be taken
            camera.startPreview();
            Util.sleep(500);
            releaseShutter();
        } else {
            Log.d(TAG, "# captureComplete");

            sendBroadcast(context, new CaptureInfo(imageFullPath.getAbsolutePath()));

            if (controllerCallback != null) {
                controllerCallback.captureComplete(null);
            }

            shutterCount = 0;
        }

        if (textureView != null) {
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

        if (autoFocusResult != null) {
            Log.w(TAG, "autofocus callback returned after timeout");
            return;
        }

        autoFocusResult = success;

        if (autoFocusResult != null && autoExposureResult != null) {
            meteringFinished(autoFocusResult);
        }
    }

    @Override
    public void onAutoFocusMoving(boolean start, Camera camera) {
        Log.d(TAG, "# onAutoFocusMoving");
    }

    private void meteringFinished(boolean afSuccessful) {
        handler.removeCallbacksAndMessages(null);

        long meteringTime = SystemClock.elapsedRealtime() - captureTimer;

        if (afSuccessful) {
            Log.d(TAG, "shutter release by autofocus success. metering time: " + meteringTime + "ms");
        } else {
            Log.d(TAG, "shutter release by autofocus timeout. metering time: " + meteringTime + "ms");
        }

        if (controllerCallback != null) {controllerCallback.cameraFocused(controller, afSuccessful);}
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
