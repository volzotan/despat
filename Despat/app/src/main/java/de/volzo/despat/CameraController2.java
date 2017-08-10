package de.volzo.despat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.volzo.despat.R.id.textureView;

/**
 * Created by volzotan on 19.12.16.
 */

public class CameraController2 {

    public static final String TAG = CameraController2.class.getName();

    Context context;
    TextureView textureView;

    CameraManager cameraManager;
    CameraCharacteristics cameraCharacteristics;
    Size imageDimension;

    CameraDevice cameraDevice;

    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraCaptureSession;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    public CameraController2(Context context, TextureView textureView) throws CameraAccessException {
        this.context = context;
        this.textureView = textureView;

        openCamera(context);
    }

    public void openCamera(Context context) throws CameraAccessException {
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            String[] cameraIdList = cameraManager.getCameraIdList();
            Log.d(TAG, "found " + cameraIdList.length + " cameras");
            String cameraId = cameraIdList[0];
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

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
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);
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

//    public void closeCamera() {
//        if (cameraDevice != null) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//        if (imageReader != null) {
//            imageReader.close();
//            imageReader = null;
//        }
//    }

}
