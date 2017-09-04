package de.volzo.despat;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.volzo.despat.support.Broadcast;
import de.volzo.despat.support.CameraAdapter;
import de.volzo.despat.support.Config;

/**
 *
 * CameraController(activity, surfaceTexture)   if surfaceTexture == null: now preview is shown
 * .setFilename(string)                         saves image as string.jpg, if empty, autogenerated 0.jpg / 1.jpg / ...
 * .startPreview()                              optional
 * .takePicture()                               starts Callback
 *
 * if shutdownAfterPictureTaken is true, no cleanup necessary. If false, .cleanup() needs to be called after takeImage callback has returned
 *
 */

public class CameraController implements CameraAdapter, Camera.PreviewCallback, Camera.PictureCallback, Camera.ShutterCallback {

    private static final String TAG = CameraController.class.getSimpleName();

    private Context context;
    private SurfaceTexture surfaceTexture;

    private Camera camera;
    private Camera.Parameters param;
    private int[] pictureSize;

    private boolean shutdownAfterPictureTaken = true;


    public CameraController(Context context, SurfaceTexture surfaceTexture) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;

        try {
            camera = Camera.open();
            openCamera();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e );
        }
    }

    // initialize camera
    private void openCamera() throws java.io.IOException {

        param = camera.getParameters();
        List<Camera.Size> pvsizes = param.getSupportedPreviewSizes();

        pictureSize = new int[] {param.getPictureSize().width, param.getPictureSize().height};
        Log.d(TAG, "supported picture size: " + pictureSize[0] + " x " + pictureSize[1]);

        int len = pvsizes.size();
        for (int i = 0; i < len; i++)
            Log.v( TAG, "camera preview format: "+pvsizes.get(i).width+"x"+pvsizes.get(i).height );

        param.setPreviewFormat( ImageFormat.NV21 );
        //param.setPreviewSize( pvsizes.get(len-1).width, pvsizes.get(len-1).height );
        param.setPreviewSize(1280, 960);

        camera.setParameters(param);
        camera.setPreviewTexture(surfaceTexture);
        // camera.setDisplayOrientation(90);

        startPreview();
    }

    public void takePhoto() {
        // camera.setOneShotPreviewCallback(this);

        camera.startPreview();
        try {
            Thread.sleep(1000); // TODO
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.takePicture(this, this, this);
    }

    public void startPreview() {
        camera.startPreview();
    }

    public void stopPreview() {
        camera.stopPreview();
    }

    public void closeCamera() {
        camera.stopPreview();
        camera.release();
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

        File dir = Config.IMAGE_FOLDER;
        File imageFullPath;
        ImageRollover imgroll = new ImageRollover(dir, ".jpg");
        imageFullPath = new File(dir, imgroll.getUnusedFilename());


        if (param.getPictureFormat() == ImageFormat.JPEG) {
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
            // try to store YUV data
            try {
                FileOutputStream fos = new FileOutputStream(imageFullPath);
                Log.d(TAG, "image format: " + param.getPictureFormat());
                YuvImage image = new YuvImage(bytes, param.getPictureFormat(), pictureSize[0], pictureSize[1], null);
                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);

                // fos.close();

            } catch (Exception e) {
                Log.e(TAG, "saving YUV failed ", e);
                return;
            }
        }

        try {
            Log.d(TAG, "picture stored:  " + imageFullPath.getCanonicalPath());

            Intent intent = new Intent(Broadcast.PICTURE_TAKEN);
            intent.putExtra("path", imageFullPath.getAbsolutePath());
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            if (shutdownAfterPictureTaken) {
                this.closeCamera();
            }
        } catch (IOException e) {
            Log.e(TAG, "accessing image path failed ", e);
            return;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.d(TAG, "preview frame");
    }

    @Override
    public void onShutter() {
        Log.d(TAG, "shutter released");
    }

    public int getState() {
        return this.STATE_DEAD;
    }
}

