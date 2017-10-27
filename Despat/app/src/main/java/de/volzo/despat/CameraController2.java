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
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.CameraAdapter;
import de.volzo.despat.support.Config;


/**
 * Created by volzotan on 19.12.16.
 */

public class CameraController2 implements CameraAdapter {

    public static final String TAG = CameraController2.class.getSimpleName();

    private Context context;
    private TextureView textureView;

    private CameraManager cameraManager;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private ImageReader imageReader;


    public CameraController2(Context context, TextureView textureView) throws Exception {
        this.context = context;
        this.textureView = textureView;

        openCamera();
    }

    public void openCamera() throws Exception {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "found " + cameraIdList.length + " cameras");
            String cameraId = cameraIdList[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new Exception("obtaining camera characteristics failed");
            }

            Size imageDimension = map.getOutputSizes(SurfaceTexture.class)[0]; // TODO: reuse this
            cameraManager.openCamera(cameraId, cameraStateCallback, null); //mBackgroundHandler);

            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        } catch (CameraAccessException e) {
            Log.d(TAG, "opening camera failed", e);
            throw e;
        } catch (SecurityException e) {
            Log.d(TAG, "opening camera failed [missing permissions]", e);
            throw e;
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onOpened");
            cameraDevice = camera;

            if (textureView != null) { // view was supplied, obviously a preview is requested
                try {
                    createPreview(textureView);
                } catch (CameraAccessException e) {
                    Log.e(TAG, "creating preview failed");
                }
            } else {
                takePhoto();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onDisconnected");
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "--> Camera: onError: " + error);
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }
    };

    private void createPreview(TextureView textureView) throws CameraAccessException {
        try {

            SurfaceTexture surfaceTexture = getSurfaceTexture(textureView);
            int width = 640; //imageDimension.getWidth();   // TODO
            int height = 480; //imageDimension.getHeight();
            surfaceTexture.setDefaultBufferSize(width, height);

            // Lowly camera API developers haven't deemed it necessary to integrate automatic screen rotation and aspect ratio
            if (textureView != null) {
                Matrix mat = new Matrix();
                mat.postScale(height / (float) width, width / (float) height);
                mat.postRotate(-90);
                mat.postTranslate(0, textureView.getHeight());
                textureView.setTransform(mat);
            }

            Surface surface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            CameraCaptureSession.StateCallback cameraCaptureSessionCallback = new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession _cameraCaptureSession) {
                    //The camera is already closed
                    if (cameraDevice == null) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview
                    cameraCaptureSession = _cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "creating capture session failed");
                }
            };

            if (cameraDevice != null) {
                cameraDevice.createCaptureSession(Arrays.asList(surface), cameraCaptureSessionCallback, null);
            } else {
                Log.e(TAG, "camera device missing");
            }

        } catch (CameraAccessException e) {
            Log.d(TAG, "creating preview failed", e);
            throw e;
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void takePhoto() {
        if (cameraDevice == null) {
            Log.e(TAG, "cameraDevice is null");
            throw new IllegalStateException();
        }

        if (cameraManager == null) { // ?
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            Log.d(TAG, "cameraManager reclaimed");
        }

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            imageReader = ImageReader.newInstance(jpegSizes[0].getWidth(), jpegSizes[0].getHeight(), ImageFormat.JPEG, 2);

            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            outputSurfaces.add(imageReader.getSurface());

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // orientation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, Surface.ROTATION_90);

            // image path
            final File imageFullPath;
            ImageRollover imgroll = new ImageRollover(Config.getImageFolder(context), Config.IMAGE_FILEEXTENSION);
            imageFullPath = imgroll.getUnusedFullFilename();

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    FileOutputStream fos = new FileOutputStream(imageFullPath);
                    try {
                        fos.write(bytes);
                        fos.close();
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                }
            };

            imageReader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i(TAG, "Saved:" + imageFullPath);

                    Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
                    intent.putExtra(Broadcast.DATA_PICTURE_PATH, imageFullPath.getAbsolutePath());
                    context.sendBroadcast(intent);

                    try {
                        if (textureView != null) {
                            createPreview(textureView);
                        }
                    } catch (CameraAccessException cae) {
                        Log.e(TAG, "captureListener failed");
                    }
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);

            Log.d(TAG, "takePicture complete");

        } catch (CameraAccessException cae) {
            Log.e(TAG, "camera access denied", cae);
        }
    }

    public void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    public int getState() {
        if (cameraDevice == null) {
            return this.STATE_DEAD;
        }

        if (textureView == null) {
            return this.STATE_EMPTY_PREVIEW;
        } else {
            return this.STATE_PREVIEW;
        }
    }

    private SurfaceTexture getSurfaceTexture(TextureView tv) {
        if (tv != null) {
            return tv.getSurfaceTexture();
        } else {
            return new SurfaceTexture(0);
        }
    }

}
