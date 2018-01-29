package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;


/**
 * Created by volzotan on 19.12.16.
 */

public class CameraController {

    public static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private ControllerCallback controllerCallback;

    private CameraManager cameraManager;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder stillRequestBuilder;
    private CaptureRequest.Builder previewRequestBuilder;
    private CameraCaptureSession captureSession;

    private CaptureRequest previewRequest;

    private Handler backgroundHandler;
    private HandlerThread backgroundThread;

    private ImageReader imageReader;
    private SurfaceTexture surfaceTexture; // no GC
    private Surface surface;

    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private int state = STATE_IDLE;

    public static final int STATE_IDLE                      = 1;
    public static final int STATE_PREVIEW                   = 2;
    public static final int STATE_WAITING_LOCK              = 3;
    public static final int STATE_WAITING_PRECAPTURE        = 4;
    public static final int STATE_WAITING_NON_PRECAPTURE    = 5;
    public static final int STATE_PICTURE_TAKEN             = 6;

    public CameraController(Context context, ControllerCallback controllerCallback, TextureView textureView) throws Exception {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;

        startBackgroundThread();
        // stopping the background thread kills the whole application when its
        // done by the ShutterService

        openCamera();
    }

    public void openCamera() throws Exception {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "found " + cameraIdList.length + " cameras");
            String cameraId = cameraIdList[0];

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            cameraManager.openCamera(cameraId, cameraStateCallback, null); //backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "opening camera failed");
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

            cameraDevice = camera;

            try {
                createCaptureSession();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }

            if (controllerCallback != null) {
                controllerCallback.cameraOpened();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onDisconnected");
            cameraOpenCloseLock.release();

            if (camera != null) {
                camera.close();
                cameraDevice = null;
            }

            if (controllerCallback != null) {
                controllerCallback.cameraFailed();
            }
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onClosed");

            //cameraOpenCloseLock.release();

            stopBackgroundThread();

            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }

            // if a textureView exists outside of the CameraController
            // closing the surfaces would cause problems
            if (textureView == null) {
                if(surface != null) {
                    surface.release();
                }

                if (surfaceTexture != null) {
                    surfaceTexture.release();
                }
            }

            if (controllerCallback != null) {
                controllerCallback.cameraClosed();
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "--> Camera: onError: " + error);
            cameraOpenCloseLock.release();

            Toast.makeText(context, "Opening Camera failed", Toast.LENGTH_SHORT).show();

            if (camera != null) {
                camera.close();
                cameraDevice = null;
            }

            if (controllerCallback != null) {
                controllerCallback.cameraFailed();
            }
        }
    };

    private void createCaptureSession() throws CameraAccessException {
        try {

            // output
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            if (jpegSizes == null) {
                Log.e(TAG, "no image size could be determined");
                return;
            }

            imageReader = ImageReader.newInstance(jpegSizes[0].getWidth(), jpegSizes[0].getHeight(), ImageFormat.JPEG, Config.NUMBER_OF_BURST_IMAGES + 2);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                    Log.d(TAG, "# onImageAvailable");

                    final ImageRollover imgroll = new ImageRollover(context);
                    File imageFullPath = imgroll.getTimestampAsFullFilename();
                    //File imageFullPath = imgroll.filenamify(Long.toString(image.getTimestamp())); // timestamp date is no unix epoch
                    if (imageFullPath == null) { // only duplicates
                        Log.e(TAG, "saving image failed. no new filename could be acquired");
                        return;
                    }

                    // backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), imageFullPath));
                    // image saving in a background thread seems not be a good idea if
                    // it's done by a service

                    Image image = reader.acquireNextImage();
                    File file = imageFullPath;

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    // BUG: on some devices (Moto Z) the buffer is empty
                    boolean empty = true;
                    for (byte b : bytes) {
                        if (b > 0) {empty = false; break;}
                    }
                    if (empty) Log.e(TAG, "empty image buffer");

                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                        if (null != output) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);

            // output surfaces
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imageReader.getSurface());

            // get empty dummy surface or surface with texture view
            surfaceTexture = getSurfaceTexture(textureView);
            int width = 640; //imageDimension.getWidth();   // TODO: drop hardcoded resolution
            int height = 480; //imageDimension.getHeight();
            surfaceTexture.setDefaultBufferSize(width, height);
            surface = new Surface(surfaceTexture);
            outputSurfaces.add(surface);

            if (textureView != null) {
                // Lowly camera API developers haven't deemed it necessary to integrate automatic screen rotation and aspect ratio
                Matrix mat = new Matrix();
                mat.postScale(height / (float) width, width / (float) height);
                mat.postRotate(-90);
                mat.postTranslate(0, textureView.getHeight());
                textureView.setTransform(mat);
            }

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            stillRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            stillRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "# onConfigured");

                    // The camera is already closed
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
                            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        captureImages();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "creating capture session failed");
                }
            }, null);
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
    }

/*
            // AF
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
            float focusdistance = 0f; //characteristics.get(characteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
            requestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusdistance);
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // orientation
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, Surface.ROTATION_90);
*/

    private CameraCaptureSession.CaptureCallback captureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

//            if (state != STATE_PREVIEW){
//                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
//
//                if (afState == null || aeState == null) {
//                    Log.e(TAG, "control state null");
//                } else {
//                    logCameraAutomaticModeState(afState, aeState);
//                }
//            }

            switch (state) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    if (Config.CAMERA_CONTROLLER_RELEASE_EARLY) {
                        state = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                        break;
                    }

                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) { // TODO
                        state = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
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

    public void captureImages() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            state = STATE_WAITING_LOCK;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {

            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Tell captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            if (null == cameraDevice) {
                Log.e(TAG, "cameraDevice missing");
                return;
            }

            // Use the same AE and AF modes as the preview.
            stillRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // stop AF/AE measurements
            captureSession.stopRepeating();
            captureSession.abortCaptures();

            final int burstLength = Config.NUMBER_OF_BURST_IMAGES; // TODO: make burstLength a function parameter

            CameraCaptureSession.CaptureCallback localCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Log.d(TAG, "# captureComplete");

                    // retrieve tag (number of image in burst sequence)
                    Object tag = request.getTag();
                    if (tag != null) {
                        int n = (int) request.getTag();
                        Log.i(TAG, "captured image [" + Integer.toString(n+1) + "/" + Config.NUMBER_OF_BURST_IMAGES + "]");
                        if (n < burstLength - 1) {
                            if (controllerCallback != null) controllerCallback.intermediateImageTaken();

                            // there are still remaining requests in the pipeline: no shutdown yet
                            return;
                        }
                    }
                    // final image of burstSequence

                    Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
                    ImageRollover imgroll = new ImageRollover(context);
                    File image = imgroll.getNewestImage();
                    intent.putExtra(Broadcast.DATA_PICTURE_PATH, image.getAbsolutePath());
                    context.sendBroadcast(intent);

                    if (controllerCallback != null) controllerCallback.finalImageTaken();

                    unlockFocus();
                }
            };

            if (burstLength == 1) {
                stillRequestBuilder.setTag(null);
                captureSession.capture(stillRequestBuilder.build(), localCaptureCallback, backgroundHandler);
            } else {

                List<CaptureRequest> captureList = new ArrayList<CaptureRequest>();
                for (int i=0; i<burstLength; i++) {
                    // attach the number of the picture in the burst sequence to the request
                    stillRequestBuilder.setTag(i);
                    CaptureRequest req = stillRequestBuilder.build();
                    captureList.add(req);
                }

                captureSession.captureBurst(captureList, localCaptureCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        Log.d(TAG, "# unlockFocus");
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

                        if (controllerCallback != null) {
                            controllerCallback.captureComplete();
                        }
                    }
                }
            }, backgroundHandler);

            // resume preview
            if (textureView != null) {
                state = STATE_PREVIEW;
                captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        Log.d(TAG, "--> closeCamera");

        try {
            cameraOpenCloseLock.acquire();

            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }

        } catch (InterruptedException ie) {
            Log.e(TAG, "lock could not be acquired", ie);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    public boolean isDead() {

        if (cameraDevice == null) {
            return true;
        } else {
            return false;
        }

    }

    private SurfaceTexture getSurfaceTexture(TextureView tv) {
        if (tv != null) {
            return tv.getSurfaceTexture();
        } else {
            return new SurfaceTexture(ThreadLocalRandom.current().nextInt(1, 1000 + 1)); //0);
        }
    }


    // additional functionality

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

    private void logHardwareLevel() {

        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = cameraManager.getCameraIdList()[0];
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

        private final Image image;
        private final File file;

        ImageSaver(Image image, File file) {
            this.image = image;
            this.file = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                image.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static abstract class ControllerCallback {

        public void cameraOpened() {}
        public void cameraClosed() {}
        public void cameraFailed() {}

        public void intermediateImageTaken() {}
        public void finalImageTaken() {}
        public void captureComplete() {}
    }
}

