package de.volzo.despat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import de.volzo.despat.support.Broadcast;;
import de.volzo.despat.support.CameraAdapter;
import de.volzo.despat.support.Config;


/**
 * Created by volzotan on 19.12.16.
 */

public class CameraController2 implements CameraAdapter {

    public static final String TAG = CameraController2.class.getName();

    private int mode;

    public static final int OPEN                = 0x0;
    public static final int OPEN_AND_PREVIEW    = 0x1;
    public static final int OPEN_AND_TAKE_PHOTO = 0x2;

    private Context context;
    private TextureView textureView;

    private CameraManager cameraManager;
    private CameraCharacteristics cameraCharacteristics;
    private Size imageDimension;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    public CameraController2(Context context, TextureView textureView, int mode) throws CameraAccessException {
        this.context = context;
        this.textureView = textureView;

        this.mode = mode;

        openCamera();
    }

    public void openCamera() throws CameraAccessException {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "found " + cameraIdList.length + " cameras");
            String cameraId = cameraIdList[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            Size[] foo = map.getOutputSizes(SurfaceTexture.class);

            cameraManager.openCamera(cameraId, cameraStateCallback, null);
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
            try {
                createPreview(textureView);
            } catch (CameraAccessException e) {
                Log.e(TAG, "creating preview failed");
            }
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "--> Camera: onDisconnected");
            cameraDevice.close();
            //cameraDevice = null; // reuse camera object or not?
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "--> Camera: onError");
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void createPreview(TextureView textureView) throws CameraAccessException {
        try {

            SurfaceTexture surfaceTexture = getSurfaceTexture(textureView);
            surfaceTexture.setDefaultBufferSize(640, 480); // imageDimension.getWidth(), imageDimension.getHeight());

//            Matrix mat = new Matrix();
//            mat.postRotate(-90.0f);
//            mat.postTranslate(0.0f, 1340.0f);
//            textureView.setTransform(mat);

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

            cameraDevice.createCaptureSession(Arrays.asList(surface), cameraCaptureSessionCallback, null);

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
            return;
        }

        if (cameraManager == null) { // ?
            cameraManager =  (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2); // TODO: number of buffers 1 or 2?
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            SurfaceTexture surfaceTexture = getSurfaceTexture(this.textureView);
            outputSurfaces.add(new Surface(surfaceTexture));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            //int rotation = Configuration.ORIENTATION_LANDSCAPE; //getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, Surface.ROTATION_90); //ORIENTATIONS.get(rotation));

            final File imageFullPath;
            ImageRollover imgroll = new ImageRollover(Config.IMAGE_FOLDER, ".jpg");
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

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.i(TAG, "Saved:" + imageFullPath);

                    Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
                    intent.putExtra("path", imageFullPath.getAbsolutePath());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    try {
                        createPreview(textureView);
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
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
//        if (imageReader != null) {
//            imageReader.close();
//            imageReader = null;
//        }
    }

    private SurfaceTexture getSurfaceTexture(TextureView tv) {
        if (tv != null) {
            return tv.getSurfaceTexture();
        } else {
            return new SurfaceTexture(0);
        }
    }

}
