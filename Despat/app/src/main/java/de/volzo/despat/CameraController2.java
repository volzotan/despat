package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.volzo.despat.persistence.Capture;
import de.volzo.despat.persistence.Event;
import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.CaptureInfo;
import de.volzo.despat.preferences.Config;
import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.DeviceInfo;
import de.volzo.despat.support.DevicePositioner;
import de.volzo.despat.support.ImageRollover;
import de.volzo.despat.support.Util;


public class CameraController2 extends CameraController {

    private static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private CameraController.ControllerCallback controllerCallback;
    private CameraConfig camconfig;

    private final CameraController controller = this;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder stillRequestBuilder;
    private CaptureRequest.Builder previewRequestBuilder;
    private CameraCaptureSession captureSession;

    private CaptureRequest previewRequest;

    private Handler backgroundHandler = null;
    private HandlerThread backgroundThread;
    Handler handler = new Handler();

    private ImageReader imageReaderJpg;
    private ImageReader imageReaderRaw;

    private final TreeMap<Integer, ImageSaver> jpgResultQueue = new TreeMap<>();
    private final TreeMap<Integer, ImageSaver> rawResultQueue = new TreeMap<>();

    private SurfaceTexture surfaceTexture; // no GC
    private Surface surface;

    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private final Object queueRemoveLock = new Object();

    private int state = STATE_CLOSED;
    private static final int STATE_CLOSED                       = 0;
    private static final int STATE_OPENED                       = 1;
    private static final int STATE_PREVIEW                      = 2;
    private static final int STATE_WAITING_FOR_3A_CONVERGENCE   = 3;

    private boolean noAF = false;
    private long captureTimer;
    private Future<Integer> devicePositionFuture;

    private long lastPreviewInfoBroadcastSent = -1;

    public CameraController2(Context context, ControllerCallback controllerCallback, TextureView textureView, CameraConfig camconfig) {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;
        this.camconfig = camconfig;

//        this.devicePositioner = new DevicePositioner(context);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        devicePositionFuture = executorService.submit(new DevicePositioner(context));

        startBackgroundThread();

//        backgroundHandler = new Handler(Looper.myLooper());
    }

    public static String[] getAvailableCameras(Context context) throws Exception {
        CameraManager cman = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        return cman.getCameraIdList();
    }

    public void openCamera() throws Exception {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = camconfig.getCameraDevice();
            characteristics = cameraManager.getCameraCharacteristics(camconfig.getCameraDevice());

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if (camconfig.isFormatRaw() && !contains(
                    characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                throw new Exception("invalid configuration: camera supports no RAW format");
            }

            cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "accessing camera failed");
            throw e;
        } catch (SecurityException e) {
            Log.w(TAG, "opening camera failed [missing permissions]");
            throw e;
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onOpened");

            cameraOpenCloseLock.release();
            state = STATE_OPENED;
            cameraDevice = camera;

            if (controllerCallback != null) {
                controllerCallback.cameraOpened();
            }

            try {
                createCaptureSession();
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
                sb.append("creating capture session failed. Error: ");
                sb.append(e.toString());
                if (e.getMessage() != null) {
                    sb.append(" | error message: ");
                    sb.append(e.getMessage());
                }
                Log.e(TAG, sb.toString());
                reportFailAndClose("creating capture session failed", e);
            }

//            if (Looper.myLooper() == Looper.getMainLooper()) {
//                Log.wtf(TAG, "camera is running on UI thread");
//            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onDisconnected");
            cameraOpenCloseLock.release();
            state = STATE_CLOSED;

            reportFailAndClose("camera: onDisconnected", null);
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onClosed");
            state = STATE_CLOSED;

            // cameraOpenCloseLock.release();

            if (imageReaderJpg != null) {
                imageReaderJpg.close();
                imageReaderJpg = null;
            }
            if (imageReaderRaw != null) {
                imageReaderRaw.close();
                imageReaderRaw = null;
            }

            // textureView is null, surfaceTexture object has been created
            // by the CameraController because no live preview is required
            if (textureView == null) {

                Log.wtf(TAG, "Surface Release");

                if(surface != null) {
                    surface.release();
                }

                if (surfaceTexture != null) {
                    surfaceTexture.release();
                }
            }

            // if a textureView exists outside of the CameraController
            // closing the surfaces would cause problems
//            if (textureView != null) {
//                Log.wtf(TAG, "Surface Release2");
//                textureView.getSurfaceTexture().release();
//            }

            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // background thread must be stopped from the main thread
                    stopBackgroundThread();

                    Log.d(TAG, "camera closing complete");

                    if (controllerCallback != null) {
                        controllerCallback.cameraClosed();
                    }
                }
            };
            mainHandler.post(runnable);
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "--> Camera: onError: " + error);
            cameraOpenCloseLock.release();

            reportFailAndClose("camera: onError", error);
        }
    };

    private void createCaptureSession() throws Exception {
        try {
            Log.d(TAG, "# createCaptureSession");

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());

            initializeImageReaders(characteristics);

            // output surfaces
            List<Surface> outputSurfaces = new ArrayList<Surface>(3);
            if (camconfig.isFormatJpg()) outputSurfaces.add(imageReaderJpg.getSurface());
            if (camconfig.isFormatRaw()) outputSurfaces.add(imageReaderRaw.getSurface());

            // get empty dummy surface or surface with texture view
            surfaceTexture = getSurfaceTexture(textureView);
            if (surfaceTexture == null) {
                Log.w(TAG, "surface texture is null!");
                throw new Exception("surface texture is null (textureView: " + textureView.toString());
            }

            // TODO: drop hardcoded resolution

            int width = 640;
            int height = 480;

            // Zoom
            if (camconfig.getZoomRegion() != null) {
                width = camconfig.getZoomRegion().width();
                height = camconfig.getZoomRegion().height();
            }
            final int stream_width = width;
            final int stream_height = height;
            surfaceTexture.setDefaultBufferSize(stream_width, stream_height);
            surface = new Surface(surfaceTexture);
            outputSurfaces.add(surface);

            int photoOrientation = 0;
            try {
                Integer result = devicePositionFuture.get(800, TimeUnit.MILLISECONDS);
                if (result != null) photoOrientation = result;
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "device positioner interrupted");
                Util.saveErrorEvent(context, "device positioner interrupted", e);
            } catch (TimeoutException e) {
                Log.w(TAG, "device positioner timeout");
                Util.saveErrorEvent(context, "device positioner timeout", e);
            } finally {
                devicePositionFuture.cancel(true);
            }
            final int position = photoOrientation;

//            Log.d(TAG, "ORIENTATION: " + photoOrientation);

            if (textureView != null) {
                // Lowly camera API developers haven't deemed it necessary to integrate automatic screen rotation and aspect ratio

                // TODO: move to a non-callback/run-on-main-thread section of code
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {

                        Matrix mat = new Matrix();
                        mat.postScale(stream_height / (float) stream_width, stream_width / (float) stream_height);

                        if (position == 0) {
                            mat.postRotate(-90);
                            mat.postTranslate(0, textureView.getHeight());
                        } else if (position == 180) {
                            mat.postRotate(90);
                            mat.postTranslate(textureView.getWidth(), 0);
                        }

                        textureView.setTransform(mat);
                    }
                });
            }

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, camconfig.getExposureCompensation());

            stillRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            if (camconfig.isFormatJpg()) stillRequestBuilder.addTarget(imageReaderJpg.getSurface());
            if (camconfig.isFormatRaw()) stillRequestBuilder.addTarget(imageReaderRaw.getSurface());

            // Rotation
            stillRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, photoOrientation);

            // Zoom
            if (camconfig.getZoomRegion() != null) {
                // TODO: check for maximum possible zoom via SCALER_AVAILABLE_MAX_DIGITAL_ZOOM
                previewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, camconfig.getZoomRegion());
                stillRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, camconfig.getZoomRegion());
            }

            // Exposure Compensation
            stillRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, camconfig.getExposureCompensation());

            // JPEG Quality
            stillRequestBuilder.set(CaptureRequest.JPEG_QUALITY, camconfig.getJpegQuality());

            // enable lens shading correction for the RAW output
            stillRequestBuilder.set(CaptureRequest.SHADING_MODE, CaptureRequest.SHADING_MODE_HIGH_QUALITY);

            // TODO: does the stillRequestBuilder really need the 3A controls?
            // metering and focusing should be done by the previewRequest
            setup3AControls(stillRequestBuilder);

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "# onConfigured");

                    if (cameraDevice == null) {
                        Log.w(TAG, "camera device already closed");
                        return;
                    }

                    captureSession = cameraCaptureSession;

                    // if a view was supplied, we want a preview
                    // if no view was supplied, no preview is needed and
                    // we just take a picture after the camera started

                    if (textureView != null) {
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            previewRequest = previewRequestBuilder.build();
                            captureSession.setRepeatingRequest(previewRequest, preCaptureCallback, backgroundHandler);
                            state = STATE_PREVIEW;
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    if (controllerCallback != null) controllerCallback.cameraReady(controller);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "creating capture session failed");
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "# onClosed CaptureSession");
                }

            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "creating capture session failed", e);
            throw e;
        }
    }

    protected void startBackgroundThread() {
        Log.d(TAG, "# startBackgroundThread");

        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        Log.d(TAG, "# stopBackgroundThread");

        if (backgroundThread == null) return;

        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "# stopBackgroundThread finished");
    }

    private CameraCaptureSession.CaptureCallback preCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

//            if (state != STATE_PREVIEW){
//                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
//
//                if (afState == null || aeState == null) {
//                    Log.d(TAG, "control state null");
//                } else {
//                    logCameraAutomaticModeState(afState, aeState);
//                }
//            }

            switch (state) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is running normally.
                    break;
                }
                case STATE_WAITING_FOR_3A_CONVERGENCE: {
                    boolean readyToCapture = false;

                    if (Config.BROADCAST_PREVIEW_DETAILS) {
                        broadcastPreviewDetails(result);
                    }

                    if (!noAF) {
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) break;

                        // If auto-focus has reached locked state, we are ready to capture
                        readyToCapture =
                                (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                        afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);

                    }

                    // If we are running on an non-legacy device, we should also wait until
                    // auto-exposure and auto-white-balance have converged as well before
                    // taking a picture.
                    if (!isLegacy()) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (aeState == null || awbState == null) {
                            break;
                        }

                        readyToCapture = readyToCapture &&
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
                                awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
                    }

                    long meteringTime = SystemClock.elapsedRealtime() - captureTimer;
                    boolean meteringOvertime = meteringTime > camconfig.getMeteringMaxTime();

                    // If we haven't finished the pre-capture sequence but have hit our maximum
                    // wait timeout, too bad! Begin capture anyway.
                    if (!readyToCapture && meteringOvertime) {
                        Log.d(TAG, "Timed out waiting for pre-capture sequence to complete.");

                        readyToCapture = true;
                    }

                    if (readyToCapture) {
                        Log.d(TAG, "metering time: " + meteringTime + "ms");

                        if (controllerCallback != null) controllerCallback.cameraFocused(controller, !meteringOvertime);

                        // After this, the camera will go back to the normal state of preview.
                        state = STATE_PREVIEW;
                    }
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    public void startMetering(Integer optionalExposureCompensation) throws IllegalAccessException {

        // TODO:
//        double random = Math.random();
//        if (random > 0.95) throw new IllegalAccessException("test");

        Log.d(TAG, "# startMetering");

        if (cameraDevice == null) {
            Log.e(TAG, "camera device missing");
            throw new IllegalAccessException("camera was closed and reopening failed");
        }

        try {
            if (!noAF) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            }

            if (!isLegacy()) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            }

            setup3AControls(previewRequestBuilder);
            state = STATE_WAITING_FOR_3A_CONVERGENCE;
            captureTimer = SystemClock.elapsedRealtime();

            if (optionalExposureCompensation != null) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, optionalExposureCompensation);
                stillRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, optionalExposureCompensation);
            }

            if (captureSession == null) {
                // TODO: try to recover instead of killing the camera
                reportFailAndClose("capture session missing", null);
            }

            captureSession.setRepeatingRequest(previewRequestBuilder.build(), preCaptureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void captureImages(String filenameSuffix) throws Exception {
        if (cameraDevice == null) {
            Log.e(TAG, "camera device missing");
            throw new IllegalAccessException("camera device missing");
        }

        // TODO:
//        double random = Math.random();
//        if (random > 0.95) throw new IllegalAccessException("test");

        captureStillPicture(false, filenameSuffix); // TODO: metering successful
    }

    private void purgeImageReaderAndSaver() {

        // clear ImageReader // hacky solution to clear the imageReader without closing and reinitializing
        int removeCounter = 0;
        try {
            for(removeCounter=0; removeCounter<camconfig.getNumberOfBurstImages()*2+1; removeCounter++) imageReaderJpg.acquireNextImage().close();
        } catch (Exception e) {
            // done
        } finally {
            if (removeCounter > 0) {
                String msg = "clearing " + removeCounter + " scheduled JPEG images from ImageReader";
                Log.w(TAG, msg);
                Util.saveEvent(context, Event.EventType.ERROR, msg);
            }
        }
        removeCounter = 0;
        try {
            for(removeCounter=0; removeCounter<camconfig.getNumberOfBurstImages()*2+1; removeCounter++) imageReaderRaw.acquireNextImage().close();
        } catch (Exception e) {
            // done
        } finally {
            if (removeCounter > 0) {
                String msg = "clearing " + removeCounter + " scheduled RAW images from ImageReader";
                Log.w(TAG, msg);
                Util.saveEvent(context, Event.EventType.ERROR, msg);
            }
        }

        // clear ResultQueue
        synchronized (queueRemoveLock) {

            if (camconfig.isFormatJpg() && jpgResultQueue != null && jpgResultQueue.size() > 0) {

                String msg = "clearing " + jpgResultQueue.size() + " scheduled JPEG images from ImageSaver";
                Log.w(TAG, msg);
                Util.saveEvent(context, Event.EventType.ERROR, msg);
                for (Integer i : jpgResultQueue.keySet()) {
                    ImageSaver saver = jpgResultQueue.get(i);
                    saver.close();
                }
                jpgResultQueue.clear();
            }

            if (camconfig.isFormatRaw() && rawResultQueue != null && rawResultQueue.size() > 0) {

                String msg = "clearing " + rawResultQueue.size() + " scheduled RAW images from ImageSaver";
                Log.w(TAG, msg);
                Util.saveEvent(context, Event.EventType.ERROR, msg);
                for (Integer i : rawResultQueue.keySet()) {
                    ImageSaver saver = rawResultQueue.get(i);
                    saver.close();
                }
                rawResultQueue.clear();
            }
        }
    }

    private void captureStillPicture(boolean meteringSuccessful, final String filenameSuffix) {
        try {
            if (cameraDevice == null) {
                Log.e(TAG, "cameraDevice missing");
                return;
            }

            if (captureSession == null) {
                Log.e(TAG, "captureSession missing");
                return;
            }


            // TODO: find a way to include metering time in the persistence.Capture object

            // Use the same AE and AF modes as the preview.
            // TODO: does this really make sense?
            // stillRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // stop AF/AE measurements
            // TODO: check if that needs to be done/makes a difference if no lockFocus call happened
            captureSession.stopRepeating();
//            captureSession.abortCaptures();

            purgeImageReaderAndSaver();

            final int burstLength = camconfig.getNumberOfBurstImages(); // TODO: make burstLength a function parameter

            CameraCaptureSession.CaptureCallback localCaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    int n = (int) request.getTag();

                    ImageRollover jpgImgroll = new ImageRollover(context, ".jpg");
                    ImageRollover rawImgroll = new ImageRollover(context, ".dng");

                    ImageSaver jpgImageSaver = jpgResultQueue.get(n);
                    ImageSaver rawImageSaver = rawResultQueue.get(n);

                    if (jpgImageSaver != null) jpgImageSaver.setFilename(jpgImgroll.getTimestampAsFullFilename(filenameSuffix));
                    if (rawImageSaver != null) rawImageSaver.setFilename(rawImgroll.getTimestampAsFullFilename(filenameSuffix));
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Log.d(TAG, "# captureComplete");

                    printCaptureStats(result);

                    // retrieve tag (number of image in burst sequence)
                    Object tag = request.getTag();
                    if (tag == null) {
                        Log.e(TAG, "capture tag missing");
                        return;
                    }

                    int n = (int) request.getTag();
                    Log.i(TAG, "captured image [" + Integer.toString(n + 1) + "/" + camconfig.getNumberOfBurstImages() + "]");

                    String filename = null;

                    synchronized (queueRemoveLock) {

                        ImageSaver jpgImageSaver = jpgResultQueue.get(n);
                        ImageSaver rawImageSaver = rawResultQueue.get(n);

                        if (jpgImageSaver != null) {
                            jpgImageSaver.captureResult = result;

                            // if the imageReader has already saved its data in the imageSaver
                            // then is now the time to actually run it

                            if (jpgImageSaver.isComplete()) {
                                jpgResultQueue.remove(n);
                                jpgImageSaver.save();
                                Log.d(TAG, "+++ jpgResultQueue size: " + jpgResultQueue.size());
                            }
                        } else {
                            Log.e(TAG, "ImageSaver has been removed, CaptureResult will be dropped");
                            Util.saveEvent(context, Event.EventType.ERROR, "ImageSaver has been removed, CaptureResult will be dropped");
                        }

                        if (rawImageSaver != null) {
                            rawImageSaver.captureResult = result;

                            if (rawImageSaver.isComplete()) {
                                rawResultQueue.remove(n);
                                rawImageSaver.save();
                                Log.d(TAG, "+++ rawResultQueue size: " + rawResultQueue.size());
                            }
                        } else {
                            Log.e(TAG, "ImageSaver has been removed, CaptureResult will be dropped");
                            Util.saveEvent(context, Event.EventType.ERROR, "ImageSaver has been removed, CaptureResult will be dropped");
                        }

                        if (jpgImageSaver != null) {
                            filename = jpgImageSaver.filename.getAbsolutePath();
                        }
                    }

                    if (n < burstLength - 1) {
                        if (controllerCallback != null) {
                            controllerCallback.intermediateImageTaken();
                        }
                    } else { // final image of burstSequence: no remaining images in the pipeline, shut it down.

                        // Caveat: image may not yet be written to disk at this point
                        // just because the broadcast is sent, the image may not yet be available

                        // TODO: move broadcast code to controllerCallback

//                        if (android.os.Build.VERSION.SDK_INT >= 28) {
//                            Map<String, CaptureResult> physicalCameraResults = result.getPhysicalCameraResults();
//                            Log.wtf(TAG, "TotalCaptureResult physicalCameraResults:");
//                            for (Map.Entry entry : physicalCameraResults.entrySet()) {
//                                Log.wtf(TAG, String.format("%s : %s", entry.getKey(), entry.getValue()));
//                            }
//                        }
//                        List<CaptureResult.Key<?>> resultKeys = result.getKeys();
//                        Log.wtf(TAG, "Result Keys:");
//                        for (CaptureResult.Key key : resultKeys) {
//                            Log.wtf(TAG, String.format("%s : %s", key, result.get(key)));
//                        }


                        Float lensAperture = result.get(CaptureResult.LENS_APERTURE);
                        Long exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME); // in ns
                        Integer sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);

                        CaptureInfo info = new CaptureInfo(filename);
                        info.setSequenceNumber(n);

                        if (lensAperture != null) {
                            info.setAperture(lensAperture);
                        }
                        if (exposureTime != null) {
                            exposureTime /= (1000 * 1000);
                            info.setExposureTime(exposureTime);
                        }
                        if (sensitivity != null) {
                            info.setIso(sensitivity);
                        }

                        sendBroadcast(context, info);

//                        if (controllerCallback != null) controllerCallback.captureComplete(info);

                        unlockFocus(info);
                    }
                }

                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {

                    Log.d(TAG, "# onCaptureFailed");
                    Util.saveErrorEvent(context,"capture failed", null);

                    // TODO: remove corresponding ImageSaver from queue
                }


                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                       int sequenceId, long frameNumber) {
                    Log.d(TAG, "# onSequenceCompleted");
                }
            };

            List<CaptureRequest> captureList = new ArrayList<CaptureRequest>();
            for (int i = 0; i < burstLength; i++) {

                // attach the number of the picture in the burst sequence to the request
                stillRequestBuilder.setTag(i);

                ImageSaver jpgImageSaver = new ImageSaver(context, characteristics, camconfig);
                ImageSaver rawImageSaver = new ImageSaver(context, characteristics, camconfig);

                jpgResultQueue.put(i, jpgImageSaver);
                rawResultQueue.put(i, rawImageSaver);

                CaptureRequest req = stillRequestBuilder.build();

                // TODO: exposure compensation

                captureList.add(req);
            }

            captureSession.captureBurst(captureList, localCaptureCallback, backgroundHandler);

            Log.d(TAG, "# captureStill");
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Util.saveErrorEvent(context, "camera access failed", e);
        }
    }

    private void unlockFocus(CaptureInfo info) {
        Log.d(TAG, "# unlockFocus");

        // TODO: evil hack for lineage on MOTO E
        if (camconfig.isEndCaptureWithoutUnlockingFocus()) {
            if (controllerCallback != null) {
                controllerCallback.captureComplete(info);
            }
            return;
        }

        if (captureSession == null || cameraDevice == null) {
            Log.e(TAG, "camera died in the background. close.");
            closeCamera();
        }

        try {
            // Reset the auto-focus trigger
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            captureSession.capture(previewRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    if (textureView == null) {
                        // no preview is needed and camera can be killed
                        // (must be happen after the cancel AF request has been processed and the image was written to disk)

                        Log.d(TAG, "# unlockedFocus CaptureCompleted");
                    }
                }
            }, backgroundHandler);

            // resume preview
            if (textureView != null) {
                state = STATE_PREVIEW;
                captureSession.setRepeatingRequest(previewRequest, preCaptureCallback, backgroundHandler);
            }

            if (controllerCallback != null) {
                controllerCallback.captureComplete(info);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Util.saveErrorEvent(context, "camera access failed", e);
        }
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                cameraOpenCloseLock.acquire();

                try {
                    if (captureSession != null) {
                        captureSession.stopRepeating();
                        captureSession.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "attempt to close captureSession of an already closed camera", e);
                } finally {
                    captureSession = null;
                }

                try {
                    if (cameraDevice != null) cameraDevice.close();
                } catch (Exception e) {
                    Log.w(TAG, "attempt to close already closed camera", e);
                } finally {
                    cameraDevice = null;
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "lock could not be acquired", ie);
                Util.saveErrorEvent(context, "lock could not be acquired", ie);
            } finally {
                cameraOpenCloseLock.release();
            }
        }
    };

    public void closeCamera() {
        Log.d(TAG, "--> closeCamera");

        if (devicePositionFuture != null && !devicePositionFuture.isCancelled()) {
            devicePositionFuture.cancel(true);
        }

        if ((jpgResultQueue != null && jpgResultQueue.size() > 0) ||
            (rawResultQueue != null && rawResultQueue.size() > 0)) {

            Log.w(TAG, "image saver queues not empty! jpg: " + jpgResultQueue.size() + " | raw: " + rawResultQueue.size());

            handler.postDelayed(closeRunnable, 1000);
        } else {
            handler.post(closeRunnable);
        }
    }

//    private void closeAndReopenImageReader() {
//        try {
//            imageReaderJpg.close();
//            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpgSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            imageReaderJpg = ImageReader.newInstance(jpgSizes[0].getWidth(), jpgSizes[0].getHeight(), ImageFormat.JPEG, Config.NUMBER_OF_BURST_IMAGES + 1);
//            imageReaderJpg.setOnImageAvailableListener(readerListenerJpg, backgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

    public boolean isDead() {
        return cameraDevice == null;
    }

    public static Size getImageSize(Context context, String cameraId) throws Exception {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        return characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)[0];
    }

    public static List<DeviceInfo.CameraInfo> getCameraInfo(Context context) throws Exception {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList = cameraManager.getCameraIdList();

        List<DeviceInfo.CameraInfo> infos = new ArrayList<>();

        for (String id : cameraIdList) {
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);

                Size resolution = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)[0];

                String direction = "";
                switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    case CameraCharacteristics.LENS_FACING_BACK: {
                        direction = "BACK";
                        break;
                    }
                    case CameraCharacteristics.LENS_FACING_FRONT: {
                        direction = "FRONT";
                        break;
                    }
                    case CameraCharacteristics.LENS_FACING_EXTERNAL: {
                        direction = "EXTERNAL";
                        break;
                    }
                    default: {
                        direction = "UNKNOWN";
                        Log.w(TAG, "unknown camera direction: " +
                                characteristics.get(CameraCharacteristics.LENS_FACING));
                        break;
                    }
                }

                boolean rawSupport = contains(characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES), CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW);

                infos.add(new DeviceInfo.CameraInfo(id, direction, resolution, rawSupport, getCameraParametersInternal(context, id)));
            } catch (Exception e) {}
        }

        return infos;
    }

    public HashMap<String, String> getCameraParameters(String cameraId) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraId != null) {
                return getCameraParametersInternal(context, cameraId);
            } else {
                Log.e(TAG, "no camera specified, using default device (0)");
                return getCameraParametersInternal(context, cameraManager.getCameraIdList()[0]);
            }
        } catch (CameraAccessException cae) {
            Log.e(TAG, "retrieving camera parameters failed", cae);
            return null;
        }
    }

    private static HashMap<String, String> getCameraParametersInternal(Context context, String cameraId) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraId);

        List<CameraCharacteristics.Key<?>> keys = c.getKeys();
        List<CaptureRequest.Key<?>> reqKeys = c.getAvailableCaptureRequestKeys();
        List<CaptureResult.Key<?>> resKeys = c.getAvailableCaptureResultKeys();

        HashMap<String, String> dict = new HashMap<String, String>();
        HashMap<Object, String> reverseKeyMap = new HashMap<Object, String>();

        Class cl = CameraCharacteristics.class;
        Field[] fields = cl.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType() == CameraCharacteristics.Key.class) {
                try {
                    reverseKeyMap.put(f.get(c), f.getName());
                } catch (IllegalAccessException iae) {
                    Log.e(TAG, "ILLEGAL ACCESS");
                }
            }
        }

        HashMap<String, HashMap<Integer, String>> interpretationMaps = new HashMap<>();

        interpretationMaps.put("CONTROL_AVAILABLE_SCENE_MODES",                     buildInterpretationMap(c, "CONTROL_SCENE_MODE"));
        interpretationMaps.put("CONTROL_AE_AVAILABLE_MODES",                        buildInterpretationMap(c, "AE_MODE"));
        interpretationMaps.put("CONTROL_AE_AVAILABLE_ANTIBANDING_MODES",            buildInterpretationMap(c, "AE_ANTIBANDING_MODE"));
        interpretationMaps.put("CONTROL_AVAILABLE_EFFECTS",                         buildInterpretationMap(c, "CONTROL_EFFECT_MODE"));
        interpretationMaps.put("CONTROL_AF_AVAILABLE_MODES",                        buildInterpretationMap(c, "CONTROL_AF_MODE"));
        interpretationMaps.put("NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES",   buildInterpretationMap(c, "NOISE_REDUCTION_MODE"));
        interpretationMaps.put("CONTROL_AWB_AVAILABLE_MODES",                       buildInterpretationMap(c, "AWB_MODE"));
        interpretationMaps.put("STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES",       buildInterpretationMap(c, "STATISTICS_FACE_DETECT_MODE"));
        interpretationMaps.put("COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES",       buildInterpretationMap(c, "COLOR_CORRECTION_ABERRATION_MODE"));
        interpretationMaps.put("INFO_SUPPORTED_HARDWARE_LEVEL",                     buildInterpretationMap(c, "INFO_SUPPORTED_HARDWARE_LEVEL"));
        interpretationMaps.put("HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES",               buildInterpretationMap(c, "HOT_PIXEL_MODE"));
        interpretationMaps.put("SENSOR_INFO_TIMESTAMP_SOURCE",                      buildInterpretationMap(c, "SENSOR_INFO_TIMESTAMP_SOURCE"));
        interpretationMaps.put("SHADING_AVAILABLE_MODES",                           buildInterpretationMap(c, "SHADING_MODE"));
        interpretationMaps.put("SENSOR_INFO_COLOR_FILTER_ARRANGEMENT",              buildInterpretationMap(c, "SENSOR_INFO_COLOR_FILTER_ARRANGEMENT"));
        interpretationMaps.put("SENSOR_AVAILABLE_TEST_PATTERN_MODES",               buildInterpretationMap(c, "SENSOR_TEST_PATTERN_MODE"));
        interpretationMaps.put("REQUEST_AVAILABLE_CAPABILITIES",                    buildInterpretationMap(c, "REQUEST_AVAILABLE_CAPABILITIES"));
        interpretationMaps.put("CONTROL_AVAILABLE_MODES",                           buildInterpretationMap(c, "CONTROL_MODE"));
        interpretationMaps.put("EDGE_AVAILABLE_EDGE_MODES",                         buildInterpretationMap(c, "EDGE_MODE"));
        interpretationMaps.put("LENS_FACING",                                       buildInterpretationMap(c, "LENS_FACING"));
        interpretationMaps.put("TONEMAP_AVAILABLE_TONE_MAP_MODES",                  buildInterpretationMap(c, "TONEMAP_MODE"));
        interpretationMaps.put("LENS_INFO_FOCUS_DISTANCE_CALIBRATION",              buildInterpretationMap(c, "LENS_INFO_FOCUS_DISTANCE_CALIBRATION"));
        interpretationMaps.put("STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES",  buildInterpretationMap(c, "STATISTICS_LENS_SHADING_MAP_MODE"));

//            for (Map.Entry<String, HashMap<Integer, String>> e : interpretationMaps.entrySet()) {
//                System.out.println(e.getKey());
//
//                for(Map.Entry<Integer, String> e2 : e.getValue().entrySet()) {
//                    System.out.println("  " + e2.getKey() + " : " + e2.getValue());
//                }
//            }

        for (CameraCharacteristics.Key<?> k : keys) {
            Object o = c.get(k);
            String fieldName = reverseKeyMap.get(k);
            HashMap<Integer, String> interpretationMap = interpretationMaps.get(fieldName);
            dict.put(fieldName, stringify(o, interpretationMap));
        }

//            for (CaptureRequest.Key<?> k : reqKeys) {
//                dict.put(k.getName(), k.toString());
//            }

//            for (CaptureResult.Key<?> k : resKeys) {
//                dict.put(k.getName(), k.toString());
//            }

        return dict;
    }

    private static HashMap<Integer, String> buildInterpretationMap(CameraCharacteristics c, String prefix) {
        HashMap<Integer, String> map = new HashMap<>();

        Class cl = CameraMetadata.class;
        Field[] fields = cl.getDeclaredFields();
        for (Field f : fields) {
            if (f.getType() == Integer.TYPE) {
                if (f.getName() != null && f.getName().contains(prefix)) {
                    try {
                        map.put(f.getInt(c), f.getName());
                    } catch (IllegalAccessException iae) {}
                }
            }
        }

        return map;
    }

    private static String stringify(Object o) {
        return stringify(o, null);
    }

    private static String stringify(Object o, HashMap<Integer, String> map) {
        if (o == null) return "";

        if (o.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < Array.getLength(o); i++) {
                sb.append(stringify(Array.get(o, i), map));
                sb.append(" ");
            }
            return sb.toString();
        }

        if (o instanceof Integer) {
            if (map != null) {
                String str = map.get(o);
                if (str != null) return str;
            }
            return o.toString();
        }

        if (o instanceof Byte) {
            return o.toString();
        }

        if (o instanceof Float) {
            return o.toString();
        }

        if (o instanceof Long) {
            return o.toString();
        }

        if (o instanceof Boolean) {
            return o.toString();
        }

        if (o instanceof Rational) {
            return o.toString();
        }

        if (o instanceof Rect) {
            return o.toString();
        }

        if (o instanceof Size) {
            Size val = (Size) o;
            return Integer.toString(val.getWidth()) + "x" + Integer.toString(val.getHeight());
        }

        if (o instanceof SizeF) {
            SizeF val = (SizeF) o;
            return Float.toString(val.getWidth()) + "x" + Float.toString(val.getHeight());
        }

        if (o instanceof Range<?>) {
            Range<?> val = (Range<?>) o;
            return "[" + stringify(val.getLower()) + ", " + stringify(val.getUpper()) + "]";
        }

        return "class: " + o.getClass().getName() + " = " + o.toString();
    }

    private void initializeImageReaders(CameraCharacteristics characteristics) {
        if (camconfig.isFormatJpg()) {
            Size[] jpgSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            if (jpgSizes == null) {
                Log.e(TAG, "no JPEG image size could be determined");
                return;
            }

            imageReaderJpg = ImageReader.newInstance(jpgSizes[0].getWidth(), jpgSizes[0].getHeight(), ImageFormat.JPEG, camconfig.getNumberOfBurstImages()*2);
            imageReaderJpg.setOnImageAvailableListener(readerListenerJpg, backgroundHandler);
        }

        if (camconfig.isFormatRaw()) {
            Size[] rawSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.RAW_SENSOR);
            if (rawSizes == null) {
                Log.e(TAG, "no RAW image size could be determined");
                return;
            }

            imageReaderRaw = ImageReader.newInstance(rawSizes[0].getWidth(), rawSizes[0].getHeight(), ImageFormat.RAW_SENSOR, camconfig.getNumberOfBurstImages()*2);
            imageReaderRaw.setOnImageAvailableListener(readerListenerRaw, backgroundHandler);
        }
    }

    private ImageReader.OnImageAvailableListener readerListenerJpg = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "# onImageAvailable JPEG");

            // backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), imageFullPath));

            synchronized (queueRemoveLock) {
                Map.Entry<Integer, ImageSaver> entry = jpgResultQueue.firstEntry();

                ImageSaver imageSaver = entry.getValue();

                try {
                    imageSaver.image = reader.acquireNextImage();
                } catch (IllegalStateException ise) {
                    String msg = "ImageReader buffer full. dropping JPEG image!";
                    Log.e(TAG, msg);
                    imageSaver.close();
                    jpgResultQueue.remove(entry.getKey());
                    imageSaver = null;

                    // TODO: probably not a good idea to send the Broadcast directly from within the
                    // camera controller
                    Intent intent = new Intent(Broadcast.ERROR_OCCURED);
                    intent.putExtra(Broadcast.DATA_DESCRIPTION, msg);
                    context.sendBroadcast(intent);

                    // error broadcast
                    Util.saveErrorEvent(context, msg, ise);
                }

                if (imageSaver != null && imageSaver.isComplete()) {
                    jpgResultQueue.remove(entry.getKey());
                    imageSaver.save();
                    Log.d(TAG, "+++ jpgResultQueue size: " + jpgResultQueue.size());
                }
            }
        }
    };

    private ImageReader.OnImageAvailableListener readerListenerRaw = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "# onImageAvailable RAW");

            synchronized (queueRemoveLock) {
                Map.Entry<Integer, ImageSaver> entry = rawResultQueue.firstEntry();

                ImageSaver imageSaver = entry.getValue();

                try {
                    imageSaver.image = reader.acquireNextImage();
                } catch (IllegalStateException ise) {
                    Log.e(TAG, "ImageReader buffer full. dropping RAW image!");
                    imageSaver.close();
                    rawResultQueue.remove(entry.getKey());
                    imageSaver = null;

                    // error broadcast
                    Util.saveErrorEvent(context, "ImageReader buffer full. dropping RAW image!", null);
                }

                if (imageSaver != null && imageSaver.isComplete()) {
                    rawResultQueue.remove(entry.getKey());
                    imageSaver.save();
                    Log.d(TAG, "+++ rawResultQueue size: " + rawResultQueue.size());
                }
            }
        }
    };

    // additional functionality

    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isLegacy() {
        return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    private void setup3AControls(CaptureRequest.Builder builder) {

        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        Float minFocusDist = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        noAF = (minFocusDist == null || minFocusDist == 0);

        if (!noAF) {
            // CONTINUOUS mode produces blurry pictures and seems to be
            // generally unreliable, default to AUTO
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(characteristics.get(   CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                                            CaptureRequest.CONTROL_AWB_MODE_AUTO)) {

            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
    }

    private static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

    private void broadcastPreviewDetails(CaptureResult r) {

        if ((System.currentTimeMillis() - lastPreviewInfoBroadcastSent) < 1000) {
            return;
        } else {
            lastPreviewInfoBroadcastSent = System.currentTimeMillis();
        }

        Float lensAperture = r.get(CaptureResult.LENS_APERTURE);
        Long exposureTime = r.get(CaptureResult.SENSOR_EXPOSURE_TIME); // in ns
        Integer sensitivity = r.get(CaptureResult.SENSOR_SENSITIVITY);

        CaptureInfo info = new CaptureInfo(null);

        if (lensAperture != null) {
            info.setAperture(lensAperture);
        }
        if (exposureTime != null) {
            exposureTime /= (1000 * 1000);
            info.setExposureTime(exposureTime);
        }
        if (sensitivity != null) {
            info.setIso(sensitivity);
        }

        Intent intent = new Intent(Broadcast.PREVIEW_INFO);
        intent.putExtra(Broadcast.DATA_IMAGE_CAPTUREINFO, info);
        context.sendBroadcast(intent);
    }

    private void printCaptureStats(CaptureResult r) {

        Float lensAperture = r.get(CaptureResult.LENS_APERTURE);
        Long exposureTime = r.get(CaptureResult.SENSOR_EXPOSURE_TIME); // in ns
        Integer sensitivity = r.get(CaptureResult.SENSOR_SENSITIVITY);

        if (exposureTime != null) exposureTime /= (1000 * 1000);

        StringBuilder sb = new StringBuilder();

        sb.append("f/: ");
        if (lensAperture != null) {
            sb.append(String.format("%.1f", lensAperture));
        } else {
            sb.append("---");
        }
        sb.append(" | ");

        sb.append("t: ");
        if (exposureTime != null) {
            sb.append(String.format("%dms", exposureTime));
        } else {
            sb.append("---");
        }
        sb.append(" | ");

        sb.append("iso: ");
        if (sensitivity != null) {
            sb.append(String.format("%d", sensitivity));
        } else {
            sb.append("---");
        }
        sb.append(" | ");

        sb.append("EV: ");
        if (lensAperture != null && exposureTime != null && sensitivity != null) {
            sb.append(String.format("%f",
                    Util.computeExposureValue(exposureTime, lensAperture, sensitivity)
            ));
        } else {
            sb.append("---");
        }

        Log.d(TAG, sb.toString());
    }

    private void logCameraAutomaticModeState(int afState, int aeState) {
        switch (afState) {
            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                Log.d(TAG, "AF: " + "active scan");
                break;
            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                Log.d(TAG, "AF: " + "focus locked");
                break;
            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
                Log.d(TAG, "AF: " + "inactive");
                break;
            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                Log.d(TAG, "AF: " + "not focus locked");
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                Log.d(TAG, "AF: " + "passive focused");
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                Log.d(TAG, "AF: " + "passive scan");
                break;
            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                Log.d(TAG, "AF: " + "passive unfocused");
                break;
            default:
                Log.d(TAG, "AF: " + "undefined");
                break;
        }

        switch (aeState) {
            case CaptureResult.CONTROL_AE_STATE_CONVERGED:
                Log.d(TAG, "AE: " + "converged");
                break;
            case CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED:
                Log.d(TAG, "AE: " + "flash required");
                break;
            case CaptureResult.CONTROL_AE_STATE_INACTIVE:
                Log.d(TAG, "AE: " + "inactive");
                break;
            case CaptureResult.CONTROL_AE_STATE_LOCKED:
                Log.d(TAG, "AE: " + "locked");
                break;
            case CaptureResult.CONTROL_AE_STATE_PRECAPTURE:
                Log.d(TAG, "AE: " + "precapture");
                break;
            case CaptureResult.CONTROL_AE_STATE_SEARCHING:
                Log.d(TAG, "AE: " + "searching");
                break;
        }
    }

    private void logHardwareLevel(String cameraId) {

        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

            int deviceLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (deviceLevel) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_3");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "Camera support level: INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED");
                    break;
                default:
                    Log.d(TAG, "Unknown INFO_SUPPORTED_HARDWARE_LEVEL: " + deviceLevel);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "HardwareLevel Error", e);
        }
    }

    private static class ImageSaver implements Runnable {
        private Context context;
        private CameraCharacteristics characteristics;
        private CameraConfig camconfig;
        CaptureResult captureResult;
        private File filename;
        Image image;

        ImageSaver(Context context, CameraCharacteristics characteristics, CameraConfig camconfig) {
            this.context = context;
            this.characteristics = characteristics;
            this.camconfig = camconfig;
//            Log.d(TAG, "# imageSaver created");
        }

        boolean isComplete() {
            return (context != null &&
                    characteristics != null &&
                    captureResult != null &&
                    filename != null &&
                    image != null);
        }

        // in case ImageSaver needs to be discarded before it's finished
        public void close() {
            if (image != null) {
                image.close();
                image = null;
            }
        }

        void setFilename(File filename) {
            this.filename = filename;

            // Log.d(TAG, "# imageSaver filename set: " + filename);
        }

        void save() {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }

        @Override
        public void run() {
            Log.d(TAG, "# imageSaver run [ " + filename + " ]");

            FileOutputStream output = null;

            switch (image.getFormat()) {
                case ImageFormat.JPEG:

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    try {
                        output = new FileOutputStream(filename);
                        output.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                        image = null;
                        closeOutput(output);
                    }

                    break;

                case ImageFormat.RAW_SENSOR:

                    DngCreator dngCreator = new DngCreator(characteristics, captureResult);

                    try {
                        output = new FileOutputStream(filename);
                        dngCreator.writeImage(output, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                        image = null;
                        closeOutput(output);
                    }

                    break;

                default:
                    Log.e(TAG, "unknown image format to save");
                    image.close();
                    image = null;
                    closeOutput(output);
                    return;
            }

            if (camconfig.isRunMediascannerAfterCapture()) {
                MediaScannerConnection.scanFile(context, new String[]{filename.getPath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {
                        // Do nothing
                    }

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i(TAG, "Scanned " + path + ":");
                        Log.i(TAG, "-> uri=" + uri);
                    }
                });
            }

            Log.d(TAG, "# imageSaver done");
        }
    }

    public static class RefCountedAutoCloseable<T extends AutoCloseable> implements AutoCloseable {
        private T mObject;
        private long mRefCount = 0;

        /**
         * Wrap the given object.
         *
         * @param object an object to wrap.
         */
        public RefCountedAutoCloseable(T object) {
            if (object == null) throw new NullPointerException();
            mObject = object;
        }

        /**
         * Increment the reference count and return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T getAndRetain() {
            if (mRefCount < 0) {
                return null;
            }
            mRefCount++;
            return mObject;
        }

        /**
         * Return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T get() {
            return mObject;
        }

        /**
         * Decrement the reference count and release the wrapped object if there are no other
         * users retaining this object.
         */
        @Override
        public synchronized void close() {
            if (mRefCount >= 0) {
                mRefCount--;
                if (mRefCount < 0) {
                    try {
                        mObject.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        mObject = null;
                    }
                }
            }
        }
    }
}

