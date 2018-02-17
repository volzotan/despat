package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.Config;
import de.volzo.despat.support.Util;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


/**
 * Created by volzotan on 19.12.16.
 */

public class CameraController2 extends CameraController {

    public static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private TextureView textureView;
    private CameraController.ControllerCallback controllerCallback;

    private CameraManager cameraManager;
    private CameraCharacteristics characteristics;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder stillRequestBuilder;
    private CaptureRequest.Builder previewRequestBuilder;
    private CameraCaptureSession captureSession;

    private CaptureRequest previewRequest;

    private Handler backgroundHandler = null;
    private HandlerThread backgroundThread;

    private ImageReader imageReader;
    private SurfaceTexture surfaceTexture; // no GC
    private Surface surface;

    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private static final long PRECAPTURE_TIMEOUT_MS = 1000;
    private int state = STATE_CLOSED;
    private static final int STATE_CLOSED = 0;
    private static final int STATE_OPENED = 1;
    private static final int STATE_PREVIEW = 2;
    private static final int STATE_WAITING_FOR_3A_CONVERGENCE = 3;

    private boolean noAF = false;
    private long captureTimer;

    public CameraController2(Context context, ControllerCallback controllerCallback, TextureView textureView) {
        this.context = context;
        this.controllerCallback = controllerCallback;
        this.textureView = textureView;

        // startBackgroundThread();
        // stopping the background thread kills the whole application when its
        // done by the ShutterService
    }

    public void openCamera() throws Exception {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "found " + cameraIdList.length + " cameras");
            String cameraId = cameraIdList[0];
            characteristics = cameraManager.getCameraCharacteristics(cameraId);

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            cameraManager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
                @Override
                public void onCameraAvailable(@NonNull String cameraId) {
                    // Log.d(TAG, "*** cameraAvailable");
                }

                @Override
                public void onCameraUnavailable(@NonNull String cameraId) {
                    // Log.d(TAG, "*** cameraUnavailable");
                }
            }, backgroundHandler);

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
            state = STATE_CLOSED;

            cameraFailed("camera: onDisconnected", null);
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onClosed");
            state = STATE_CLOSED;

            // cameraOpenCloseLock.release();

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

//            Toast.makeText(context, "Opening Camera failed", Toast.LENGTH_SHORT).show();

            cameraFailed("camera: onError", error);
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
            setup3AControls(stillRequestBuilder);

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
                            captureSession.setRepeatingRequest(previewRequest, preCaptureCallback, backgroundHandler);
                            state = STATE_PREVIEW;
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            captureImages();
                        } catch (IllegalAccessException e) {
                            Log.e(TAG, "capture failed: ", e);
                        }
                    }
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

    protected void startBackgroundThread(Looper looper) {
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

    private CameraCaptureSession.CaptureCallback preCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

            if (state != STATE_PREVIEW){
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);

                if (afState == null || aeState == null) {
                    Log.d(TAG, "control state null");
                } else {
                    logCameraAutomaticModeState(afState, aeState);
                }
            }

            switch (state) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is running normally.
                    break;
                }
                case STATE_WAITING_FOR_3A_CONVERGENCE: {
                    boolean readyToCapture = true;
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

                    // If we haven't finished the pre-capture sequence but have hit our maximum
                    // wait timeout, too bad! Begin capture anyway.
                    if (!readyToCapture && check3ATimer()) {
                        Log.w(TAG, "Timed out waiting for pre-capture sequence to complete.");
                        readyToCapture = true;
                    }

                    if (readyToCapture) {
                        captureStillPicture();

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

    public void captureImages() throws IllegalAccessException {
        if (cameraDevice == null) {
            Log.e(TAG, "camera device missing. trying to reopen");
            try {
                openCamera();
            } catch (Exception e) {
                Log.e(TAG, "reopening failed", e);
                throw new IllegalAccessException("camera was closed and reopening failed");
            }
        }

        lockFocus();
    }

    private void lockFocus() {
        Log.d(TAG, "# lockFocus");

        try {
            if (!noAF) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            }

            if (!isLegacy()) {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            }

            state = STATE_WAITING_FOR_3A_CONVERGENCE;
            start3ATimer();

            captureSession.setRepeatingRequest(previewRequestBuilder.build(), preCaptureCallback, backgroundHandler);
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

                    // beware, the image may not be written to disk at this point
                    // TODO: in case a lot of images are saved, this takes several seconds...
//                    ImageRollover imgroll = new ImageRollover(context);
//                    File image = imgroll.getNewestImage();
                    sendBroadcast(context, "foo"); //image.getAbsolutePath());

                    if (controllerCallback != null) controllerCallback.finalImageTaken();

                    unlockFocus();
                }

                public void onCaptureFailed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    Log.d(TAG, "# onCaptureFailed");
                }


                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                       int sequenceId, long frameNumber) {
                    Log.d(TAG, "# onSequenceCompleted");
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

                    // TODO: exposure compensation

                    captureList.add(req);
                }

                captureSession.captureBurst(captureList, localCaptureCallback, backgroundHandler);
            }

            Log.d(TAG, "# captureStill");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        Log.d(TAG, "# unlockFocus");

        // evil hack for lineage on MOTO E
        if (Config.END_CAPTURE_WITHOUT_UNLOCKING_FOCUS) {
            if (controllerCallback != null) {
                controllerCallback.captureComplete();
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

                        if (controllerCallback != null) {
                            controllerCallback.captureComplete();
                        }
                    }
                }
            }, backgroundHandler);

            // resume preview
            if (textureView != null) {
                state = STATE_PREVIEW;
                captureSession.setRepeatingRequest(previewRequest, preCaptureCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        Log.d(TAG, "--> closeCamera");

        try {
            cameraOpenCloseLock.acquire();

            try {
                if (captureSession != null) {
                    captureSession.stopRepeating();
                    captureSession.close();
                    captureSession = null;
                }
            } catch (CameraAccessException cae) {
                cae.printStackTrace();
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (cameraDevice != null) {
                        cameraDevice.close();
                        cameraDevice = null;
                    }
                }
            }, 1000); // TODO: delay really necessary?

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

    public HashMap<String, String> getCameraParameters() {

        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[0]);

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
                        System.out.println("ILLEGAL ACCESS");
                    }
                }
            }

            HashMap<String, HashMap<Integer, String>> interpretationMaps = new HashMap<>();
            interpretationMaps.put("CONTROL_AVAILABLE_SCENE_MODES", buildInterpretationMap(c, "CONTROL_SCENE_MODE"));
            interpretationMaps.put("CONTROL_AE_AVAILABLE_MODES", buildInterpretationMap(c, "AE_MODE"));
            interpretationMaps.put("CONTROL_AE_AVAILABLE_ANTIBANDING_MODES", buildInterpretationMap(c, "AE_ANTIBANDING_MODE"));
            interpretationMaps.put("CONTROL_AVAILABLE_EFFECTS", buildInterpretationMap(c, "CONTROL_EFFECT_MODE"));
            interpretationMaps.put("CONTROL_AF_AVAILABLE_MODES", buildInterpretationMap(c, "CONTROL_AF_MODE"));
            interpretationMaps.put("NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES", buildInterpretationMap(c, "NOISE_REDUCTION_MODE"));
            interpretationMaps.put("CONTROL_AWB_AVAILABLE_MODES", buildInterpretationMap(c, "AWB_MODE"));
            interpretationMaps.put("STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES", buildInterpretationMap(c, "STATISTICS_FACE_DETECT_MODE"));
            interpretationMaps.put("COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES", buildInterpretationMap(c, "COLOR_CORRECTION_ABERATION_MODE"));


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

        } catch (CameraAccessException cae) {
            Log.e(TAG, "retrieving camera parameters failed", cae);
            return null;
        }
    }

    private HashMap<Integer, String> buildInterpretationMap(CameraCharacteristics c, String prefix) {
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

    private String stringify(Object o) {
        return stringify(o, null);
    }

    private String stringify(Object o, HashMap<Integer, String> map) {
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

    // additional functionality

    private boolean isLegacy() {
        return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    private void setup3AControls(CaptureRequest.Builder builder) {

        // Enable auto-magical 3A run by camera device
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        Float minFocusDist = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        noAF = (minFocusDist == null || minFocusDist == 0);

        if (!noAF) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (contains(characteristics.get(
                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (contains(characteristics.get(
                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
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

    private void start3ATimer() {
        captureTimer = SystemClock.elapsedRealtime();
    }

    private boolean check3ATimer() {
        return (SystemClock.elapsedRealtime() - captureTimer) > PRECAPTURE_TIMEOUT_MS;
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
}

