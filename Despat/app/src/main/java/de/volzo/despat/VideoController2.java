package de.volzo.despat;

public class VideoController2 {
    public VideoController2() {

    }
}


//package de.volzo.despat;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.hardware.camera2.CameraAccessException;
//import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
//import android.hardware.camera2.CameraDevice;
//import android.hardware.camera2.CameraManager;
//import android.hardware.camera2.params.StreamConfigurationMap;
//import android.media.MediaRecorder;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.support.annotation.NonNull;
//import android.util.Log;
//import android.util.Size;
//import android.view.Surface;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import de.volzo.despat.support.DevicePositioner;
//import de.volzo.despat.support.ImageRollover;
//import de.volzo.despat.support.Util;
//
//public class VideoController2 {
//
//    private static final String TAG = VideoController2.class.getSimpleName();
//
//    Context context;
//
//    private Semaphore cameraOpenCloseLock = new Semaphore(1);
//
//    private Integer sensorOrientation;
//    private Size previewSize;
//    private Size videoSize;
//
//    private HandlerThread backgroundThread;
//    private Handler backgroundHandler;
//
//    private CameraDevice cameraDevice;
//    private MediaRecorder mediaRecorder;
//
//    private Future<Integer> devicePositionFuture;
//
//    public VideoController2(Context context) {
//        this.context = context;
//
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        devicePositionFuture = executorService.submit(new DevicePositioner(context));
//    }
//
//    public void init() {
//        startBackgroundThread();
//    }
//
//    public void close() {
//        stopBackgroundThread();
//    }
//
//    public void start() {
//        openCamera(600, 480);
//    }
//
//    public void stop() {
//
//    }
//
//    private void openCamera(int width, int height) {
////        final Activity activity = getActivity();
//
//        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//        try {
//            Log.d(TAG, "tryAcquire");
//            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw new RuntimeException("Time out waiting to lock camera opening.");
//            }
//            String cameraId = manager.getCameraIdList()[0];
//
//            // Choose the sizes for camera preview and video recording
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//            if (map == null) {
//                throw new RuntimeException("Cannot get available preview/video sizes");
//            }
//            videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
//            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, videoSize);
//
////            int orientation = getResources().getConfiguration().orientation;
////            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
////                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
////            } else {
////                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
////            }
////            configureTransform(width, height);
//            mediaRecorder = new MediaRecorder();
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            // Currently an NPE is thrown when the Camera2API is used but not supported on the
//            // device this code runs.
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Interrupted while trying to lock camera opening.");
//        }
//    }
//
//    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//
//        @Override
//        public void onOpened(@NonNull CameraDevice camera) {
//            cameraDevice = camera;
//            cameraOpenCloseLock.release();
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice camera) {
//            cameraOpenCloseLock.release();
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice camera, int error) {
//            cameraOpenCloseLock.release();
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//
//    };
//
//    private static Size chooseVideoSize(Size[] choices) {
//        for (Size size : choices) {
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
//        }
//        Log.e(TAG, "Couldn't find any suitable video size");
//        return choices[choices.length - 1];
//    }
//
//    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            if (option.getHeight() == option.getWidth() * h / w &&
//                    option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
//        }
//
//        // Pick the smallest of those, assuming we found any
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new VideoController.CompareSizesByArea());
//        } else {
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            return choices[0];
//        }
//    }
//
////    private void configureTransform(int viewWidth, int viewHeight) {
////        Activity activity = getActivity();
////        if (null == mTextureView || null == mPreviewSize || null == activity) {
////            return;
////        }
////        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
////        Matrix matrix = new Matrix();
////        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
////        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
////        float centerX = viewRect.centerX();
////        float centerY = viewRect.centerY();
////        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
////            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
////            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
////            float scale = Math.max(
////                    (float) viewHeight / mPreviewSize.getHeight(),
////                    (float) viewWidth / mPreviewSize.getWidth());
////            matrix.postScale(scale, scale, centerX, centerY);
////            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
////        }
////        textureView.setTransform(matrix);
////    }
//
//    private void startBackgroundThread() {
//        backgroundThread = new HandlerThread("CameraBackground");
//        backgroundThread.start();
//        backgroundHandler = new Handler(backgroundThread.getLooper());
//    }
//
//    private void stopBackgroundThread() {
//        backgroundThread.quitSafely();
//        try {
//            backgroundThread.join();
//            backgroundThread = null;
//            backgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void setUpMediaRecorder() throws IOException {
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        ImageRollover imgroll = new ImageRollover(context, ".mp4");
//        mediaRecorder.setOutputFile(imgroll.getUnusedFullFilename().toString());
//        mediaRecorder.setVideoEncodingBitRate(10000000);
//        mediaRecorder.setVideoFrameRate(30);
//        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//
//        int rotation = 0;
//        try {
//            Integer result = devicePositionFuture.get(500, TimeUnit.MILLISECONDS);
//            if (result != null) rotation = result;
//        } catch (InterruptedException | ExecutionException e) {
//            Log.w(TAG, "device positioner interrupted");
//            Util.saveErrorEvent(context, "device positioner interrupted", e);
//        } catch (TimeoutException e) {
//            Log.w(TAG, "device positioner timeout");
//            Util.saveErrorEvent(context, "device positioner timeout", null);
//        } finally {
//            devicePositionFuture.cancel(true);
//        }
//
//        mediaRecorder.setOrientationHint(rotation);
//
//        mediaRecorder.prepare();
//    }
//
////    private void startPreview() {
////        if (null == cameraDevice || !textureView.isAvailable() || null == previewSize) {
////            return;
////        }
////        try {
////            closePreviewSession();
////            SurfaceTexture texture = mTextureView.getSurfaceTexture();
////            assert texture != null;
////            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
////            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
////
////            Surface previewSurface = new Surface(texture);
////            mPreviewBuilder.addTarget(previewSurface);
////
////            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
////                    new CameraCaptureSession.StateCallback() {
////
////                        @Override
////                        public void onConfigured(@NonNull CameraCaptureSession session) {
////                            mPreviewSession = session;
////                            updatePreview();
////                        }
////
////                        @Override
////                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
////                            Activity activity = getActivity();
////                            if (null != activity) {
////                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
////                            }
////                        }
////                    }, backgroundHandler);
////        } catch (CameraAccessException e) {
////            e.printStackTrace();
////        }
////    }
//
//    private void startRecordingVideo() {
//        if (null == cameraDevice || !textureView.isAvailable() || null == previewSize) {
//            return;
//        }
//        try {
//            closePreviewSession();
//            setUpMediaRecorder();
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(peviewSize.getWidth(), previewSize.getHeight());
//            captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            List<Surface> surfaces = new ArrayList<>();
//
//            // Set up Surface for the camera preview
//            Surface previewSurface = new Surface(texture);
//            surfaces.add(previewSurface);
//            captureBuilder.addTarget(previewSurface);
//
//            // Set up Surface for the MediaRecorder
//            Surface recorderSurface = mediaRecorder.getSurface();
//            surfaces.add(recorderSurface);
//            captureBuilder.addTarget(recorderSurface);
//
//            // Start a capture session
//            // Once the session starts, we can update the UI and start recording
//            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
//
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    previewSession = cameraCaptureSession;
//                    updatePreview();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // UI
//                            buttonVideo.setText("stop");
//                            mIsRecordingVideo = true;
//
//                            // Start recording
//                            mediaRecorder.start();
//                        }
//                    });
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Activity activity = getActivity();
//                    if (null != activity) {
//                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }, backgroundHandler);
//        } catch (CameraAccessException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closePreviewSession() {
//        if (previewSession != null) {
//            previewSession.close();
//            previewSession = null;
//        }
//    }
//
//    private void stopRecordingVideo() {
//        mediaRecorder.stop();
//        mediaRecorder.reset();
//        startPreview();
//    }
//
//    private void closeCamera() {
//        try {
//            cameraOpenCloseLock.acquire();
//            closePreviewSession();
//            if (null != cameraDevice) {
//                cameraDevice.close();
//                cameraDevice = null;
//            }
//            if (null != mediaRecorder) {
//                mediaRecorder.release();
//                mediaRecorder = null;
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException("Interrupted while trying to lock camera closing.");
//        } finally {
//            cameraOpenCloseLock.release();
//        }
//    }
//}
